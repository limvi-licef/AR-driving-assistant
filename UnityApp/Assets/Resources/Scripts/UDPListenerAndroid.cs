using UnityEngine;
using System.Collections;
using System.Threading;
using System.Net.Sockets;
using System.IO;
using System.Net;
using System.Text;
using System;

/// <summary>
/// UDP listener for Unity on Android
/// </summary>
public class UDPListenerAndroid : MonoBehaviour {

    //Deal with the udp responses
    public SpeedCounter speedCounter;
    public UserManager userManager;
    public RetroactionScript retroaction;
    public UDPSender UDPSender;

#if UNITY_ANDROID

    string msg = "";
    Thread mThread;
    bool mRunning;
    UdpClient udpClient = null;

    void Start()
    {
        //Start listener thread
        mRunning = true;
        ThreadStart ts = new ThreadStart(Server);
        mThread = new Thread(ts);
        mThread.Start();
    }

    public void stopListening()
    {
        mRunning = false;
    }

    /// <summary>
    /// Bind UdpClient to port and start listening to requests
    /// </summary>
    void Server()
    {
        try
        {
            IPEndPoint endPoint = new IPEndPoint(IPAddress.Any, Config.Communication.PORT);
            udpClient = new UdpClient(endPoint);
            IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);

            while (mRunning)
            {
                Byte[] receiveBytes = udpClient.Receive(ref RemoteIpEndPoint);

                //save ip address in case it is different from default
                UDPSender.IP = RemoteIpEndPoint.Address.ToString().Trim();

                msg = Encoding.ASCII.GetString(receiveBytes);
                HandleRequest(msg);
            }
        }
        catch (ThreadAbortException)
        {
            print("exception");
        }
        finally
        {
            mRunning = false;
            udpClient.Close();
        }
    }

    void OnApplicationQuit()
    {
        // stop listening thread
        stopListening();
        // wait for listening thread to terminate (max. 500ms)
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
            //called on main thread since textures can only be updated on main thread
            UnityMainThreadDispatcher.Instance().Enqueue(DispatchEvent(eventResponse.eventType, eventResponse.message));
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

    private IEnumerator DispatchEvent(string eventType, string message)
    {
        EventManager.SendEvent(eventType, message);
        yield return null;
    }

#endif
}

