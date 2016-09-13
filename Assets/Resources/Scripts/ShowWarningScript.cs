using UnityEngine;
using System.Collections;
using HoloToolkit.Unity;
using UnityEngine.UI;

public class ShowWarningScript : MonoBehaviour
{
    public GameObject container;
    GameObject warningDisplay;
    RawImage sign;
    RawImage arrow;

    private const float DisplayTime = 5f;
    private const string WarningLeft = "Attention sur votre gauche";
    private const string WarningRight = "Attention sur votre droite";

    void Start()
    {
        //warningDisplay = (Instantiate(Resources.Load("Prefabs/WarningDisplay"), Vector3.zero, Quaternion.identity) as GameObject);
        //warningDisplay.transform.SetParent(gameObject.transform, false);
        //sign = warningDisplay.transform.Find("Sign").GetComponent<RawImage>();
        //arrow = warningDisplay.transform.Find("Arrow").GetComponent<RawImage>();
        //warningDisplay.SetActive(false);
    }

    void OnEnable()
    {
        EventManager.OnTrigger += ShowWarning;
    }

    void OnDisable()
    {
        EventManager.OnTrigger -= ShowWarning;
    }

    void ShowWarning(string prefabName)
    {
        if(warningDisplay) { Destroy(warningDisplay); }

        SetupWarning(prefabName);

        UAudioManager.Instance.PlayEvent("WarningBeep", this.gameObject.GetComponent<AudioSource>());
        Destroy(warningDisplay, DisplayTime);
    }

    void SetupWarning(string prefabName)
    {
        warningDisplay = (Instantiate(Resources.Load("Prefabs/" + prefabName), Vector3.zero, Quaternion.identity) as GameObject);
        warningDisplay.transform.SetParent(gameObject.transform, false);
    }

}