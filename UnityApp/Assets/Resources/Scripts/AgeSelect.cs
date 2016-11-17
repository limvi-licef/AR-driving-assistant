using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using System;

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
