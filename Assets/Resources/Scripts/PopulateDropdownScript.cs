using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.UI;

public class PopulateDropdownScript : MonoBehaviour {

    public int lowerLimit;
    public int upperLimit;
    public Text Placeholder;

    void Start () {
        List<string> list = new List<string>();
        for(int i = lowerLimit; i <= upperLimit; i++){ list.Add(i.ToString()); }
        var dropdown = GetComponent<Dropdown>();
        dropdown.options.Clear();
        foreach (string option in list)
        {
            dropdown.options.Add(new Dropdown.OptionData(option));
        }

        //Unselect first option
        Dropdown d = gameObject.GetComponent<Dropdown>();
        d.options.Add(new Dropdown.OptionData() { text = "" });
        d.value = d.options.Count - 1;
        d.options.RemoveAt(d.options.Count - 1);

        Placeholder.gameObject.SetActive(true);
    }

}
