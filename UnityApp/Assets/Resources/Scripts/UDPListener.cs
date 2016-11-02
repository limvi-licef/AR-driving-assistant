using UnityEngine;
using System;
using System.IO;

#if !UNITY_EDITOR
    using Windows.Networking.Sockets;
#endif

/*
 * Adapted from https://forums.hololens.com/discussion/comment/9837
 * Listens on port 12345 for incoming udp packets
 * Packets must contain a string defining the Event name (see EventManager.cs) and the message to display, each separated by a ';' character
 * ex: "Information;TextToBeDisplayed"
 */

public class UDPListener : MonoBehaviour
{
#if !UNITY_EDITOR
    private DatagramSocket socket;
#endif

    private const string PORT = "12345";

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
            await socket.BindEndpointAsync(null, "12345");
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

        string[] tokens = message.Split(';');
        UnityEngine.WSA.Application.InvokeOnAppThread(() =>
        {
            EventManager.SendEvent(tokens[0], tokens[1]);
        }, false);
    }
#endif
}
