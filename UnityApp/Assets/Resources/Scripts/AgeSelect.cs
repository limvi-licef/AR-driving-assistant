using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using System;

/// <summary>
/// Allows user to select age with only taps
/// </summary>
/// <remarks>
/// Use intended for HoloLens since there is no keyboard
/// </remarks>
public class AgeSelect : MonoBehaviour {

    public Text AgeLabel;

	void Start () {
	}
	
    public void IncreaseAge()
    {
        int age = Int32.Parse(AgeLabel.text);
        ++age;
        AgeLabel.text = age.ToString();
    }

    public void DecreaseAge()
    {
        int age = Int32.Parse(AgeLabel.text);
        --age;
        AgeLabel.text = age.ToString();
    }
}
