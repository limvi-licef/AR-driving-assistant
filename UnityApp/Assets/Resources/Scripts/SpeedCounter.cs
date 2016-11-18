using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class SpeedCounter : MonoBehaviour {

    public Text SpeedCounterText;

    public void SetSpeed(string speed)
    {
        SpeedCounterText.text = speed;
    } 
}
