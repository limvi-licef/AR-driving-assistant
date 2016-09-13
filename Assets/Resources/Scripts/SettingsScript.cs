using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using HoloToolkit.Unity;
using System.Collections.Generic;

public class SettingsScript : MonoBehaviour {

    public Slider volumeSlider;
    public Slider sizeSlider;
    public GameObject ui;

    void Start()
    {
        AudioListener.volume = 0.5f;
    }

    void Update()
    {
        //Set UI scale
        ui.transform.localScale = new Vector3(sizeSlider.value, sizeSlider.value, ui.transform.localScale.z);
    }

    public void SetVolume()
    {
       AudioListener.volume = volumeSlider.value;
    }
}
