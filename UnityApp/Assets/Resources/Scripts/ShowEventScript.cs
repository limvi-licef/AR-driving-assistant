using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
#if UNITY_WSA_10_0 && !UNITY_EDITOR
    using HoloToolkit.Unity;
#endif

/// <summary>
/// Script to manage the way events are displayed to the user
/// </summary>
public class ShowEventScript : MonoBehaviour
{
    GameObject eventDisplay;
    GameObject welcomeScreen;
    public UserManager UserManager;

    void OnEnable()
    {
        EventManager.OnTrigger += ShowEvent;
    }

    void OnDisable()
    {
        EventManager.OnTrigger -= ShowEvent;
    }

    /// <summary>
    /// Show an event on the application screen for DisplayTime seconds
    /// </summary>
    /// <param name="e">The event to show</param>
    void ShowEvent(Event e)
    {
        if(eventDisplay) { Destroy(eventDisplay); }
        SetupEvent(e);
        Destroy(eventDisplay, Config.EVENT_DISPLAY_TIME);
    }

    /// <summary>
    /// Instantiate the event received in param
    /// </summary>
    /// <param name="e">The event to instantiate</param>
    void SetupEvent(Event e)
    {
#if UNITY_WSA_10_0 && !UNITY_EDITOR
        eventDisplay = (Instantiate(Resources.Load("Prefabs/" + e.Prefab), Vector3.zero, Quaternion.identity) as GameObject);
        UAudioManager.Instance.PlayEvent(e.Sound, this.gameObject.GetComponent<AudioSource>());
#endif
#if UNITY_ANDROID
        eventDisplay = (Instantiate(Resources.Load("Prefabs/Android/" + e.Prefab), Vector3.zero, Quaternion.identity) as GameObject);
        GetComponent<AudioSource>().PlayOneShot(Resources.Load("Sounds/" + e.Sound) as AudioClip, AudioListener.volume);
#endif
        eventDisplay.GetComponentInChildren<Text>().text = e.Text;
        eventDisplay.GetComponentInChildren<RawImage>().texture = Resources.Load("Images/" + e.Icon) as Texture;
        eventDisplay.GetComponentInChildren<RawImage>().color = e.Color;
        eventDisplay.transform.SetParent(gameObject.transform, false);
    }

    /// <summary>
    /// Displays a welcome screen for DisplayTime seconds each time the application screen is displayed
    /// </summary>
    public void ShowWelcomeScreen()
    {
        if (welcomeScreen) { Destroy(welcomeScreen); }
        SetupWelcomeScreen();
        Destroy(welcomeScreen, Config.EVENT_DISPLAY_TIME);
    }

    /// <summary>
    /// Instantiate the welcome screen
    /// </summary>
    void SetupWelcomeScreen()
    {
#if UNITY_WSA_10_0 && !UNITY_EDITOR
        welcomeScreen = (Instantiate(Resources.Load("Prefabs/WelcomeScreen"), Vector3.zero, Quaternion.identity) as GameObject);
#endif
#if UNITY_ANDROID
        welcomeScreen = (Instantiate(Resources.Load("Prefabs/Android/WelcomeScreen"), Vector3.zero, Quaternion.identity) as GameObject);
#endif
        welcomeScreen.transform.SetParent(gameObject.transform, false);
        welcomeScreen.GetComponentInChildren<Text>().text = UserManager.userId;
    }

}