using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

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

    public void SetRides(List<string> rides)
    {
        textZone.text += "\n\n";
        foreach(string line in rides)
        {
            textZone.text += line;
        }
    }

    private void requestRides ()
    {
        JsonClasses.JsonRequestLastKnownRides ridesRequest = new JsonClasses.JsonRequestLastKnownRides();
        ridesRequest.requestType = JsonClasses.NewUserRequest;
        ridesRequest.userId = UserManager.userId;
        UDPSender.SendJSON(ridesRequest);
    }
}
