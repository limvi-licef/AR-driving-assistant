using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class DropdownToInputFieldScript : MonoBehaviour {

    public InputField inputfield;

    void OnEnable()
    {
        gameObject.GetComponent<Dropdown>().onValueChanged.AddListener(ChangeInputText);
    }

    void OnDisable()
    {
        gameObject.GetComponent<Dropdown>().onValueChanged.RemoveListener(ChangeInputText);
    }

    void ChangeInputText(int i)
    {
        inputfield.text = gameObject.GetComponentInChildren<Text>().text;
    }

}
