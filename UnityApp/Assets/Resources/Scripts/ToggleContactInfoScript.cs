using UnityEngine;
using System.Collections;

public class ToggleContactInfoScript : MonoBehaviour {

    bool toggleBool = false;

    void Start()
    {
        gameObject.SetActive(false);
    }

    public void ToggleContactInfo()
    {
        if(!toggleBool) gameObject.GetComponentInParent<RectTransform>().SetAsLastSibling();
        toggleBool = !toggleBool;
        gameObject.SetActive(toggleBool);
    }
}
