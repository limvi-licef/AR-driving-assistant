﻿using UnityEngine;
using System;
using System.IO;

#if UNITY_WSA_10_0 && !UNITY_EDITOR
    using Windows.Networking.Sockets;
    using Windows.Storage.Streams;
#endif

/// <summary>
/// Sends UDP packets containing json requests
/// </summary>
public class UDPSender : MonoBehaviour {

    //default ip address for android hotspot
    private string ip = "192.168.43.1";
    private string port = TCPListenerHoloLens.PORT;

    public string IP
    {
        get { return ip; }
        set { ip = value; }
    }

    public string Port
    {
        get { return port; }
        set { port = value; }
    }

#if UNITY_WSA_10_0 && !UNITY_EDITOR
    /// <summary>
    /// Connects to the Android app and send json
    /// </summary>
    /// <param name="jsonString">The json string to send</param>
    private async void ConnectAndSend(string jsonString)
    {
        StreamSocket socket = new StreamSocket();
        await socket.ConnectAsync(new Windows.Networking.HostName(IP), Port);
        DataWriter writer = new DataWriter(socket.OutputStream);
        writer.WriteString(jsonString);
        await writer.StoreAsync();
    }
#endif

#if UNITY_ANDROID
    /// <summary>
    /// Connects to the Android app and send json
    /// </summary>
    /// <param name="jsonString">The json string to send</param>
    private void ConnectAndSend(string jsonString)
    {
        //DO ANDROID TCP
    }
#endif

    /// <summary>
    /// Send async json request
    /// </summary>
    /// <param name="json">The json request to send</param>
    public void SendJSON(JsonClasses.JsonRequest json)
    {
#if UNITY_WSA_10_0 && !UNITY_EDITOR
        ConnectAndSend(JsonUtility.ToJson(json));
#endif
#if UNITY_ANDROID
        ConnectAndSend(JsonUtility.ToJson(json));
#endif
    }


}
