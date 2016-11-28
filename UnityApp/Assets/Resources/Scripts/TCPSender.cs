using UnityEngine;
using System;
using System.IO;
using System.Collections.Generic;
using System.Collections;

#if UNITY_ANDROID
    using System.Net.Sockets;
#endif

#if UNITY_WSA_10_0 && !UNITY_EDITOR
    using Windows.Networking.Sockets;
    using Windows.Storage.Streams;
#endif

/// <summary>
/// Sends TCP packets containing json requests
/// </summary>
public class TCPSender : MonoBehaviour {

    //default ip address for android hotspot
    private string ip = Config.Communication.DEFAULT_IP;
    private int port = Config.Communication.PORT;

    public string IP
    {
        get { return ip; }
        set { ip = value; }
    }

    public int Port
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
        await socket.ConnectAsync(new Windows.Networking.HostName(IP), Port.ToString());
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
    private IEnumerator ConnectAndSend(string jsonString)
    {
        TcpClient client = new TcpClient();
        client.Connect(IP, Port);
        if (client.Connected)
        {
            NetworkStream stream = client.GetStream();
            StreamWriter writer = new StreamWriter(stream);
            writer.Write(jsonString);
            writer.Close();
        }
        client.Close();
        yield return null;
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
        StartCoroutine(ConnectAndSend(JsonUtility.ToJson(json)));
#endif
    }


}
