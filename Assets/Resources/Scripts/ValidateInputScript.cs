using UnityEngine;
using UnityEngine.UI;
using System;

public class ValidateInputScript : MonoBehaviour {

    public Dropdown IdDropdown;
    public Text Label;
    public Text Placeholder;
    public UserManager UserManager;

    void Start()
    {
        gameObject.SetActive(true);
    }

    public void ValidateInput()
    {
        if(ValidateID())
        {
            UserManager.SetUserId(IdDropdown.itemText.text);
            gameObject.SetActive(false);
        }
    }

	bool ValidateID()
    {
        if(String.IsNullOrEmpty(Label.text))
        {
            Placeholder.color = Color.red;
            return false;
        }
        return true;
    }

}
