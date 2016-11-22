using UnityEngine;
using System.Collections;
using UnityEngine.UI;

/// <summary>
/// Script to update the SpeedCounter on the application screen
/// </summary>
public class SpeedCounter : MonoBehaviour {

    public Text SpeedCounterText;

    public void SetSpeed(string speed)
    {
        SpeedCounterText.text = speed;
    } 
}
