using UnityEngine;
using System;
using System.IO;

#if !UNITY_EDITOR
    using Windows.Networking.Sockets;
#endif

/// <summary>
/// Listens on port 12345 for incoming udp packets and redirects requests to the adequate class
/// </summary>
/// <remarks>
/// Adapted from https://forums.hololens.com/discussion/comment/9837
/// Packets must contain a json string that respect the classes in JsonClasses.cs
/// </remarks>
public class UDPListener : MonoBehaviour
{
    //Deal with the udp responses
    public SpeedCounter speedCounter;
    public UserManager userManager;
    public RetroactionScript retroaction;

#if !UNITY_EDITOR
    private DatagramSocket socket;
#endif

    public static readonly string PORT = "12345";

    void Start()
    {
        Debug.Log(Application.platform);
#if !UNITY_EDITOR
        Server();
#endif
    }

    void Update()
    {

    }

#if !UNITY_EDITOR
    private async void Server()
    {
        socket = new DatagramSocket();
        socket.MessageReceived += Socket_MessageReceived;

        try
        {
            await socket.BindEndpointAsync(null, PORT);
        }
        catch (Exception e)
        {
            Debug.Log(e.ToString());
            Debug.Log(SocketError.GetStatus(e.HResult).ToString());
            return;
        }
    }
#endif

#if !UNITY_EDITOR
    private async void Socket_MessageReceived(Windows.Networking.Sockets.DatagramSocket sender,
        Windows.Networking.Sockets.DatagramSocketMessageReceivedEventArgs args)
    {
        //Read the message that was received from the UDP echo client.
        Stream streamIn = args.GetDataStream().AsStreamForRead();
        StreamReader reader = new StreamReader(streamIn);
        string message = await reader.ReadLineAsync();
        Debug.Log(message);
        JsonClasses.JsonResponse response = new JsonClasses.JsonResponse();
        JsonUtility.FromJsonOverwrite(message, response);

        UnityEngine.WSA.Application.InvokeOnAppThread(() =>
        {
            if (response.requestType.Equals(JsonClasses.SpeedResponse))
            {
                JsonClasses.JsonResponseSpeed speedResponse = new JsonClasses.JsonResponseSpeed();
                JsonUtility.FromJsonOverwrite(message, speedResponse);
                speedCounter.SetSpeed(speedResponse.speedText);
            }
            else if (response.requestType.Equals(JsonClasses.EventResponse))
            {
                JsonClasses.JsonResponseEvent eventResponse = new JsonClasses.JsonResponseEvent();
                JsonUtility.FromJsonOverwrite(message, eventResponse);
                EventManager.SendEvent(eventResponse.eventType, eventResponse.message);
            }
            else if (response.requestType.Equals(JsonClasses.UsersResponse))
            {
                JsonClasses.JsonResponseUsers usersResponse = new JsonClasses.JsonResponseUsers();
                JsonUtility.FromJsonOverwrite(message, usersResponse);
                userManager.Users = usersResponse.users;
            }
            else if (response.requestType.Equals(JsonClasses.NewUserResponse))
            {
                JsonClasses.JsonResponseInsert insertResponse = new JsonClasses.JsonResponseInsert();
                JsonUtility.FromJsonOverwrite(message, insertResponse);
                if(insertResponse.status)
                {
                    userManager.AddNewUser(insertResponse.newUser);
                }
                else
                {
                    userManager.DisplayError("Impossible de créer un nouvel utilisateur");
                }
            }
            else if (response.requestType.Equals(JsonClasses.RidesResponse))
            {
                JsonClasses.JsonResponseLastKnown ridesResponse = new JsonClasses.JsonResponseLastKnown();
                JsonUtility.FromJsonOverwrite(message, ridesResponse);
                retroaction.SetRides(ridesResponse.rides);
            }
            else 
            {
                Debug.Log("UNKNOWN RESPONSE TYPE");
            }
        }, false);

    }
#endif
}
