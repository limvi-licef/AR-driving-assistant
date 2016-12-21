using UnityEngine;
using System;
using System.IO;

#if UNITY_WSA_10_0 && !UNITY_EDITOR
    using Windows.Networking.Sockets;
#endif

/// <summary>
/// Listens for incoming udp packets and redirects messages to the adequate method
/// </summary>
/// <remarks>
/// Adapted from https://forums.hololens.com/discussion/comment/9837
/// Packets must contain a json string that respect the classes in JsonClasses.cs
/// </remarks>
public class UDPListenerHoloLens : MonoBehaviour
{
    //Deal with the udp responses
    public SpeedCounter speedCounter;
    public UserManager userManager;
    public RetroactionScript retroaction;
    public UDPSender UDPSender;

#if UNITY_WSA_10_0 && !UNITY_EDITOR
    private DatagramSocket socket;
#endif

    void Start()
    {
#if UNITY_WSA_10_0 && !UNITY_EDITOR
        Server();
#endif
    }

    void Update()
    {

    }

    /// <summary>
    /// Bind listener socket to port
    /// </summary>
#if UNITY_WSA_10_0 && !UNITY_EDITOR
    private async void Server()
    {
        socket = new DatagramSocket();
        socket.MessageReceived += Socket_MessageReceived;

        try
        {
            await socket.BindEndpointAsync(null, Config.Communication.PORT.ToString());
        }
        catch (Exception e)
        {
            Debug.Log(e.ToString());
            Debug.Log(SocketError.GetStatus(e.HResult).ToString());
            return;
        }
    }
#endif

#if UNITY_WSA_10_0 && !UNITY_EDITOR
    private async void Socket_MessageReceived(
                Windows.Networking.Sockets.DatagramSocket sender, 
                Windows.Networking.Sockets.DatagramSocketMessageReceivedEventArgs args)
    {
        //save ip address in case it is different from default
        UDPSender.IP = args.RemoteAddress.ToString();

        //Read the json message that was received from the Android client
        Stream streamIn = args.GetDataStream().AsStreamForRead();
        StreamReader reader = new StreamReader(streamIn);
        string message = await reader.ReadLineAsync();

        // Do nothing if empty message
        if(String.IsNullOrEmpty(message)) 
        {
            return;
        }
        
        //Deserialize json
        JsonClasses.JsonResponse response = new JsonClasses.JsonResponse();
        JsonUtility.FromJsonOverwrite(message, response);

        //Handle json message
        UnityEngine.WSA.Application.InvokeOnAppThread(() =>
        {
            if (response.requestType.Equals(Config.Communication.SPEED_RESPONSE))
            {
                JsonClasses.JsonResponseSpeed speedResponse = new JsonClasses.JsonResponseSpeed();
                JsonUtility.FromJsonOverwrite(message, speedResponse);
                speedCounter.SetSpeed(speedResponse.speedText);
            }
            else if (response.requestType.Equals(Config.Communication.EVENT_RESPONSE))
            {
                JsonClasses.JsonResponseEvent eventResponse = new JsonClasses.JsonResponseEvent();
                JsonUtility.FromJsonOverwrite(message, eventResponse);
                EventManager.SendEvent(eventResponse.eventType, eventResponse.message);
            }
            else if (response.requestType.Equals(Config.Communication.USERS_RESPONSE))
            {
                JsonClasses.JsonResponseUsers usersResponse = new JsonClasses.JsonResponseUsers();
                JsonUtility.FromJsonOverwrite(message, usersResponse);
                userManager.Users = usersResponse.users;
            }
            else if (response.requestType.Equals(Config.Communication.NEW_USER_RESPONSE))
            {
                JsonClasses.JsonResponseInsert insertResponse = new JsonClasses.JsonResponseInsert();
                JsonUtility.FromJsonOverwrite(message, insertResponse);
                if(insertResponse.status)
                {
                    userManager.AddNewUser(insertResponse.newUser);
                }
                else
                {
                    userManager.DisplayError(Config.ErrorMessages.NEW_USER_FAILURE);
                }
            }
            else if (response.requestType.Equals(Config.Communication.RIDES_RESPONSE))
            {
                JsonClasses.JsonResponseLastKnown ridesResponse = new JsonClasses.JsonResponseLastKnown();
                JsonUtility.FromJsonOverwrite(message, ridesResponse);
                if (ridesResponse.status.Equals(Config.Communication.LAST_KNOWN_RIDES_SUCCESS))
                {
                    retroaction.SetRides(ridesResponse.rides);
                }
                else
                {
                    retroaction.SetErrorText(ridesResponse.status);
                }
            }
        }, false);

    }
#endif
}
