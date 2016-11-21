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
    public SpeedCounter speedCounter;
    public UserManager userManager;
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
                JsonClasses.JsonResponseSpeed speed = new JsonClasses.JsonResponseSpeed();
                JsonUtility.FromJsonOverwrite(message, speed);
                speedCounter.SetSpeed(speed.speedText);
            }
            else if (response.requestType.Equals(JsonClasses.EventResponse))
            {
                JsonClasses.JsonResponseEvent eventResponse = new JsonClasses.JsonResponseEvent();
                JsonUtility.FromJsonOverwrite(message, eventResponse);
                EventManager.SendEvent(eventResponse.eventType, eventResponse.message);
            }
            else if (response.requestType.Equals(JsonClasses.UsersResponse))
            {
                JsonClasses.JsonResponseUsers users = new JsonClasses.JsonResponseUsers();
                JsonUtility.FromJsonOverwrite(message, users);
                userManager.Users = users.users;
            }
            else if (response.requestType.Equals(JsonClasses.NewUserResponse))
            {
                JsonClasses.JsonResponseInsert insert = new JsonClasses.JsonResponseInsert();
                JsonUtility.FromJsonOverwrite(message, insert);
                if(insert.status)
                {
                    userManager.AddNewUser(insert.newUser);
                }
                else
                {
                    userManager.DisplayError("Impossible de créer un nouvel utilisateur");
                }
            }
            else if (response.requestType.Equals(JsonClasses.RidesResponse))
            {
                JsonClasses.JsonResponseLastKnown rides = new JsonClasses.JsonResponseLastKnown();
                JsonUtility.FromJsonOverwrite(message, rides);
            }
            else 
            {
                Debug.Log("UNKNOWN RESPONSE TYPE");
            }
        }, false);

    }
#endif
}
