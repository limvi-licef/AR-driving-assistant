using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class SettingsScript : MonoBehaviour {

    public Slider volumeSlider;

    void Start()
    {
        AudioListener.volume = 0.5f;
    }

    public void SetVolume()
    {
       AudioListener.volume = volumeSlider.value;
    }
}
