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
        EventManager.OnClicked += ShowWarning;
    }

    void OnDisable()
    {
        EventManager.OnClicked -= ShowWarning;
    }

    void ShowWarning(string prefabName)
    {
        if(warningDisplay) { Destroy(warningDisplay); }
        SetupWarning(prefabName);
        //warningDisplay.SetActive(true);
        this.gameObject.transform.SetAsLastSibling();
        UAudioManager.Instance.PlayEvent("WarningBeep", this.gameObject.GetComponent<AudioSource>());
        Destroy(warningDisplay, DisplayTime);
        //StartCoroutine(DisableAfterTime());
    }

    void SetupWarning(string prefabName)
    {
        //string direction = "left";
        //sign.texture = resources.load("images/warning_icon") as texture;
        //sign.color = color.red;
        //sign.recttransform.sizedelta = new vector2(96f, 96f);
        //arrow.texture = resources.load("images/" + direction + "_arrow_icon") as texture;
        //arrow.color = color.black;
        //arrow.recttransform.sizedelta = new vector2(32f, 32f);
        //arrow.transform.localposition = new vector3(1, transform.localposition.y, transform.localposition.z);

        warningDisplay = (Instantiate(Resources.Load("Prefabs/" + prefabName), Vector3.zero, Quaternion.identity) as GameObject);
        warningDisplay.transform.SetParent(gameObject.transform, false);
    }

    //IEnumerator DisableAfterTime()
    //{
    //    yield return new WaitForSeconds(DisplayTime);
    //    warningDisplay.SetActive(false);
    //}
}