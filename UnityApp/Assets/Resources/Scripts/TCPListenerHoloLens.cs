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

    public static readonly string PORT = "12345";

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
        Debug.Log("SERVER 1");
        socketListener = new Windows.Networking.Sockets.StreamSocketListener();
        Debug.Log("SERVER 2");
        socketListener.ConnectionReceived += SocketListener_ConnectionReceived;

        try
        {
            Debug.Log("SERVER 3");
            await socketListener.BindServiceNameAsync(PORT); 
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
        Debug.Log("MESSAGE RECEIVED 1");

        //Read the json message that was received from the Android client
        Stream streamIn = args.Socket.InputStream.AsStreamForRead();
        Debug.Log("MESSAGE RECEIVED 2");
        StreamReader reader = new StreamReader(streamIn);
        Debug.Log("MESSAGE RECEIVED 3");
        string message = await reader.ReadLineAsync();
        Debug.Log("MESSAGE RECEIVED 4");

        Debug.Log(message);
        // Do nothing if empty message
        if(String.IsNullOrEmpty(message)) 
        {
            return;
        }
        
        //Deserialize json
        JsonClasses.JsonResponse response = new JsonClasses.JsonResponse();
        JsonUtility.FromJsonOverwrite(message, response);
        Debug.Log("MESSAGE RECEIVED 5");

        //Handle json message
        UnityEngine.WSA.Application.InvokeOnAppThread(() =>
        {
            if (response.requestType.Equals(JsonClasses.SpeedResponse))
            {
                Debug.Log("MESSAGE RECEIVED SPEED");
                JsonClasses.JsonResponseSpeed speedResponse = new JsonClasses.JsonResponseSpeed();
                JsonUtility.FromJsonOverwrite(message, speedResponse);
                speedCounter.SetSpeed(speedResponse.speedText);
            }
            else if (response.requestType.Equals(JsonClasses.EventResponse))
            {
                Debug.Log("MESSAGE RECEIVED EVENT");
                JsonClasses.JsonResponseEvent eventResponse = new JsonClasses.JsonResponseEvent();
                JsonUtility.FromJsonOverwrite(message, eventResponse);
                EventManager.SendEvent(eventResponse.eventType, eventResponse.message);
            }
            else if (response.requestType.Equals(JsonClasses.UsersResponse))
            {
                Debug.Log("MESSAGE RECEIVED USERS");
                JsonClasses.JsonResponseUsers usersResponse = new JsonClasses.JsonResponseUsers();
                JsonUtility.FromJsonOverwrite(message, usersResponse);
                userManager.Users = usersResponse.users;
            }
            else if (response.requestType.Equals(JsonClasses.NewUserResponse))
            {
                Debug.Log("MESSAGE RECEIVED NEW USER");
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
                Debug.Log("MESSAGE RECEIVED RIDES");
                JsonClasses.JsonResponseLastKnown ridesResponse = new JsonClasses.JsonResponseLastKnown();
                JsonUtility.FromJsonOverwrite(message, ridesResponse);
                retroaction.SetRides(ridesResponse.rides);
            }
        }, false);

    }
#endif
}
