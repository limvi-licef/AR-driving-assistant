using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class RetroactionScript : MonoBehaviour {

    public UDPSender UDPSender;
    public UserManager UserManager;

    void OnEnable()
    {
        requestRides();
    }

    public void SetRides(List<string> rides)
    {
        var textZone = GetComponentInChildren<Text>();
        textZone.text = "\\n";
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
