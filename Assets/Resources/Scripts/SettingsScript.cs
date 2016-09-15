using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using HoloToolkit.Unity;
using System.Collections.Generic;

public class SettingsScript : MonoBehaviour {

    public Slider VolumeSlider;
    public Slider SizeSlider;
    public GameObject UI;

    void Start()
    {
        AudioListener.volume = 0.5f;
    }

    void Update()
    {
        //Set UI scale
        UI.transform.localScale = new Vector3(SizeSlider.value, SizeSlider.value, UI.transform.localScale.z);
    }

    public void SetVolume()
    {
       AudioListener.volume = VolumeSlider.value;
    }
}
