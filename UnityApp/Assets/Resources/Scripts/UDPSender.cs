using UnityEngine;
using System;
using System.IO;

#if !UNITY_EDITOR
    using Windows.Networking.Sockets;
    using Windows.Storage.Streams;
#endif

/// <summary>
/// Sends UDP packets containing json requests
/// </summary>
public class UDPSender : MonoBehaviour {

#if !UNITY_EDITOR
    private DatagramSocket socket;
#endif

    //default ip address for android hotspot
    private string ip = "192.168.43.1";
    private string port = UDPListener.PORT;

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

    /// <summary>
    /// Connects to the Android app and send json
    /// </summary>
    /// <param name="jsonString">The json string to send</param>
#if !UNITY_EDITOR
    private async void ConnectAndSend(string jsonString)
    {
        socket = new DatagramSocket();
        await socket.ConnectAsync(new Windows.Networking.HostName(IP), Port);
        DataWriter writer = new DataWriter(socket.OutputStream);
        writer.WriteString(jsonString);
        await writer.StoreAsync();
    }
#endif

    /// <summary>
    /// Send async json request
    /// </summary>
    /// <param name="json">The json request to send</param>
    public void SendJSON(JsonClasses.JsonRequest json)
    {
#if !UNITY_EDITOR
        ConnectAndSend(JsonUtility.ToJson(json));
#endif
    }
	

}
