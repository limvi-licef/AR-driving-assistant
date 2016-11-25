using UnityEngine;
using System;
using System.IO;

#if UNITY_WSA_10_0 && !UNITY_EDITOR
    using Windows.Networking.Sockets;
#endif

/// <summary>
/// Listens on port 12345 for incoming tcp connections and redirects messages to the adequate method
/// </summary>
/// <remarks>
/// Adapted from https://www.tutorialspoint.com/windows10_development/windows10_development_networking.htm
/// Packets must contain a json string that respect the classes in JsonClasses.cs
/// </remarks>
public class TCPListenerHoloLens : MonoBehaviour
{
    //Deal with the tcp responses
    public SpeedCounter speedCounter;
    public UserManager userManager;
    public RetroactionScript retroaction;

#if UNITY_WSA_10_0 && !UNITY_EDITOR
    private Windows.Networking.Sockets.StreamSocketListener socketListener;
#endif

    void Start()
    {
        Debug.Log(Application.platform);
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
        socketListener = new Windows.Networking.Sockets.StreamSocketListener();
        socketListener.ConnectionReceived += SocketListener_ConnectionReceived;

        try
        {
            await socketListener.BindServiceNameAsync(Config.Communication.PORT.ToString()); 
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
    private async void SocketListener_ConnectionReceived(
                Windows.Networking.Sockets.StreamSocketListener sender, 
                Windows.Networking.Sockets.StreamSocketListenerConnectionReceivedEventArgs args)
    {
        //Read the json message that was received from the Android client
        Stream streamIn = args.Socket.InputStream.AsStreamForRead();
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
