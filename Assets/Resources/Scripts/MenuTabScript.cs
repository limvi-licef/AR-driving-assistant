using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class MenuTabScript : MonoBehaviour {

    public Button TabButton;
    public Transform TabDisplay;
    public GameObject TabOutline;
    public bool IsHomeMenu;

    void OnEnable()
    {
        TabButton.onClick.AddListener(ChangeTab);
        if(IsHomeMenu) EventManager.OnTabClick += ChangeTab;
    }

    void OnDisable()
    {
        TabButton.onClick.RemoveListener(ChangeTab);
        if (IsHomeMenu) EventManager.OnTabClick -= ChangeTab;
    }

    public void ChangeTab()
    {
        TabOutline.GetComponent<RawImage>().transform.SetParent(gameObject.transform, false);
        TabOutline.GetComponent<RawImage>().color = gameObject.GetComponentInChildren<RawImage>().color;
        TabDisplay.SetAsLastSibling();
    }

}
