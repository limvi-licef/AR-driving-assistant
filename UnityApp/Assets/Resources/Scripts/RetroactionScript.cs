using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

/// <summary>
/// Script to manage the retroaction results text field
/// </summary>
public class RetroactionScript : MonoBehaviour {

    public TCPSender TCPSender;
    public UserManager UserManager;

    private Text textZone;

    void Start()
    {
        textZone = GetComponentInChildren<Text>();
        SetDefaultText();
    }

    void OnEnable()
    {
        RequestRides();
    }

    /// <summary>
    /// Adds the last known ride dates to the gameobject's children text field
    /// </summary>
    /// <param name="rides">The ride dates to display</param>
    public void SetRides(List<string> rides)
    {
        SetDefaultText();
        foreach(string line in rides)
        {
            textZone.text += line + "\n";
        }
    }

    /// <summary>
    /// Displays an error message in the text zone if the request failed
    /// </summary>
    /// <param name="message">The message  to display</param>
    public void SetErrorText(string message)
    {
        SetDefaultText();
        textZone.text = message;
    }

    /// <summary>
    /// Resets the text zone to its default state
    /// </summary>
    private void SetDefaultText()
    {
        textZone.text = Config.LAST_KNOWN_RIDES_TITLE;
        textZone.text += "\n\n";
    }

    /// <summary>
    /// Sends a request to fetch the last known ride dates from the Android app
    /// </summary>
    private void RequestRides ()
    {
        JsonClasses.JsonRequestLastKnownRides ridesRequest = new JsonClasses.JsonRequestLastKnownRides();
        ridesRequest.requestType = Config.Communication.LAST_KNOWN_RIDES_REQUEST;
        ridesRequest.userId = UserManager.userId;
        TCPSender.SendJSON(ridesRequest);
    }

}
