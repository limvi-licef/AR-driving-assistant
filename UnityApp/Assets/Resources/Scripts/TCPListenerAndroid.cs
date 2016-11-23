using UnityEngine;
using System.Collections;
using System.Threading;
using System.Net.Sockets;
using System.IO;
using System.Net;

/// <summary>
/// TCP listener for Unity on Android
/// </summary>
public class TCPListenerAndroid : MonoBehaviour {

    //Deal with the tcp responses
    public SpeedCounter speedCounter;
    public UserManager userManager;
    public RetroactionScript retroaction;

#if UNITY_ANDROID

    public static readonly int PORT = 12345;

    string msg = "";
    Thread mThread;
    bool mRunning;
    TcpListener tcp_Listener = null;

    void Start()
    {
        mRunning = true;
        ThreadStart ts = new ThreadStart(Server);
        mThread = new Thread(ts);
        mThread.Start();
        print("Thread done...");
    }

    public void stopListening()
    {
        mRunning = false;
    }

    void Server()
    {
        try
        {
            tcp_Listener = new TcpListener(IPAddress.Any, PORT);
            tcp_Listener.Start();
            print("Server Start");
            while (mRunning)
            {
                // check if new connections are pending, if not, be nice and sleep 100ms
                if (!tcp_Listener.Pending())
                {
                    Thread.Sleep(100);
                }
                else
                {
                    print("1");
                    TcpClient client = tcp_Listener.AcceptTcpClient();
                    print("2");
                    NetworkStream ns = client.GetStream();
                    print("3");
                    StreamReader reader = new StreamReader(ns);
                    print("4");
                    msg = reader.ReadLine();
                    print(msg);

                    HandleRequest(msg);

                    reader.Close();
                    client.Close();
                }
            }
        }
        catch (ThreadAbortException)
        {
            print("exception");
        }
        finally
        {
            mRunning = false;
            tcp_Listener.Stop();
        }
    }

    void OnApplicationQuit()
    {
        // stop listening thread
        stopListening();
        // wait fpr listening thread to terminate (max. 500ms)
        mThread.Join(500);
    }

    void HandleRequest(string message)
    {
        //Deserialize json
        JsonClasses.JsonResponse response = new JsonClasses.JsonResponse();
        JsonUtility.FromJsonOverwrite(message, response);

        //Handle json message
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
                if (insertResponse.status)
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
        }, false);
    }
#endif
}

