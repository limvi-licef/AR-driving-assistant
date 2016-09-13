using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class MenuTabScript : MonoBehaviour {

    public Button tabButton;
    public Transform tabDisplay;
    public GameObject tabOutline;

    void OnEnable()
    {
        tabButton.onClick.AddListener(ChangeTab);
        EventManager.OnTabClick += ChangeTab;
    }

    void OnDisable()
    {
        tabButton.onClick.RemoveListener(ChangeTab);
        EventManager.OnTabClick -= ChangeTab;
    }

    public void ChangeTab()
    {
        tabOutline.GetComponent<RawImage>().transform.SetParent(gameObject.transform, false);
        tabOutline.GetComponent<RawImage>().color = gameObject.GetComponentInChildren<RawImage>().color;
        tabDisplay.SetAsLastSibling();
    }

}
