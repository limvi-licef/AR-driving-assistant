using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class MenuTabScript : MonoBehaviour {

    public Button TabButton;
    public Transform TabDisplay;
    public GameObject TabOutline;

    void OnEnable()
    {
        TabButton.onClick.AddListener(ChangeTab);
        EventManager.OnTabClick += ChangeTab;
    }

    void OnDisable()
    {
        TabButton.onClick.RemoveListener(ChangeTab);
        EventManager.OnTabClick -= ChangeTab;
    }

    public void ChangeTab()
    {
        TabOutline.GetComponent<RawImage>().transform.SetParent(gameObject.transform, false);
        TabOutline.GetComponent<RawImage>().color = gameObject.GetComponentInChildren<RawImage>().color;
        TabDisplay.SetAsLastSibling();
    }

}
