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
    }

    public void stopListening()
    {
        mRunning = false;
    }

    void Server()
    {
        try
        {
            tcp_Listener = new TcpListener(IPAddress.Any, Config.Communication.PORT);
            tcp_Listener.Start();
            while (mRunning)
            {
                // check if new connections are pending, if not, be nice and sleep 100ms
                if (!tcp_Listener.Pending())
                {
                    Thread.Sleep(100);
                }
                else
                {
                    TcpClient client = tcp_Listener.AcceptTcpClient();
                    NetworkStream ns = client.GetStream();
                    StreamReader reader = new StreamReader(ns);
                    msg = reader.ReadLine();
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
            if (insertResponse.status)
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
    }
#endif
}

