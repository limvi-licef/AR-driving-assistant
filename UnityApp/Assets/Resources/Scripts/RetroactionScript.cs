using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

/// <summary>
/// Script to manage the retroaction results text field
/// </summary>
public class RetroactionScript : MonoBehaviour {

    public UDPSender UDPSender;
    public UserManager UserManager;

    private Text textZone;
    private readonly string title = "Derniers Trajets";

    void Start()
    {
        textZone = GetComponentInChildren<Text>();
    }

    void OnEnable()
    {
        textZone.text = title;
        requestRides();
    }

    /// <summary>
    /// Adds the last known ride dates to the gameobject's children text field
    /// </summary>
    /// <param name="rides">The ride dates to display</param>
    public void SetRides(List<string> rides)
    {
        textZone.text += "\n\n";
        foreach(string line in rides)
        {
            textZone.text += line;
        }
    }

    /// <summary>
    /// Sends a request to fetch the last known ride dates from the Andrdoi app
    /// </summary>
    private void requestRides ()
    {
        JsonClasses.JsonRequestLastKnownRides ridesRequest = new JsonClasses.JsonRequestLastKnownRides();
        ridesRequest.requestType = JsonClasses.LastKnownRidesRequest;
        ridesRequest.userId = UserManager.userId;
        UDPSender.SendJSON(ridesRequest);
    }
}
