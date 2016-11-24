using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using System.Collections.Generic;

/// <summary>
/// Script to manage the settings panel
/// </summary>
public class SettingsScript : MonoBehaviour {

    public Slider VolumeSlider;
    public Slider SizeSlider;
    public GameObject UI;

    void Start()
    {
        AudioListener.volume = Config.DEFAULT_VOLUME;
    }

    void Update()
    {
#if UNITY_WSA_10_0
        //HoloLens only : Update UI scale
        UI.transform.localScale = new Vector3(SizeSlider.value, SizeSlider.value, UI.transform.localScale.z);
#endif
    }

    /// <summary>
    /// Update the volume value
    /// </summary>
    public void SetVolume()
    {
       AudioListener.volume = VolumeSlider.value;
    }
}
