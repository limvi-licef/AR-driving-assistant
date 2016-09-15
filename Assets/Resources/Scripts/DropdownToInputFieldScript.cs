using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class DropdownToInputFieldScript : MonoBehaviour {

    public InputField Inputfield;

    void Start()
    {
        //Unselect first option
        Dropdown d = gameObject.GetComponent<Dropdown>();
        d.options.Add(new Dropdown.OptionData() { text = "" });
        d.value = d.options.Count - 1;
        d.options.RemoveAt(d.options.Count - 1);
    }

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
        Inputfield.text = gameObject.GetComponentInChildren<Text>().text;
        gameObject.GetComponentInChildren<Outline>().effectColor = Color.black;
        Inputfield.GetComponentInChildren<Outline>().effectColor = Color.black;
    }

}
