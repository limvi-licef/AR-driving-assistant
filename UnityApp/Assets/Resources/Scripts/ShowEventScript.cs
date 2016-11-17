using UnityEngine;
using HoloToolkit.Unity;
using UnityEngine.UI;

public class ShowEventScript : MonoBehaviour
{
    GameObject eventDisplay;
    GameObject welcomeScreen;
    public UserManager UserManager;

    private const float DisplayTime = 5f;

    void OnEnable()
    {
        EventManager.OnTrigger += ShowEvent;
    }

    void OnDisable()
    {
        EventManager.OnTrigger -= ShowEvent;
    }

    void ShowEvent(Event e)
    {
        if(eventDisplay) { Destroy(eventDisplay); }
        SetupEvent(e);
        Destroy(eventDisplay, DisplayTime);
    }

    void SetupEvent(Event e)
    {
        eventDisplay = (Instantiate(Resources.Load("Prefabs/" + e.Prefab), Vector3.zero, Quaternion.identity) as GameObject);
        eventDisplay.transform.SetParent(gameObject.transform, false);
        eventDisplay.GetComponentInChildren<Text>().text = e.Text;
        eventDisplay.GetComponentInChildren<RawImage>().texture = Resources.Load("Images/" + e.Icon) as Texture;
        eventDisplay.GetComponentInChildren<RawImage>().color = e.Color;
        UAudioManager.Instance.PlayEvent(e.Sound, this.gameObject.GetComponent<AudioSource>());
    }

    public void ShowWelcomeScreen()
    {
        if (welcomeScreen) { Destroy(welcomeScreen); }
        SetupWelcomeScreen();
        Destroy(welcomeScreen, DisplayTime);
    }

    void SetupWelcomeScreen()
    {
        welcomeScreen = (Instantiate(Resources.Load("Prefabs/WelcomeScreen"), Vector3.zero, Quaternion.identity) as GameObject);
        welcomeScreen.transform.SetParent(gameObject.transform, false);
        welcomeScreen.GetComponentInChildren<Text>().text = UserManager.GetUserId();
    }

}