using UnityEngine;
using UnityEngine.UI;
using System;

public class ValidateInputScript : MonoBehaviour {

    public Dropdown AgeDropdown;
    public Dropdown IdDropdown;
    public InputField IdInputfield;
    public UserManager UserManager;

    public void ValidateInput()
    {
        if(ValidateID() & ValidateAge())
        {
            UserManager.SetUserId(IdInputfield.text);
            UserManager.SetUserAge(AgeDropdown.value);
            gameObject.SetActive(false);
        }
    }

	bool ValidateID()
    {
        if(String.IsNullOrEmpty(IdInputfield.text))
        {
            IdInputfield.GetComponent<Outline>().effectColor = Color.red;
            IdDropdown.GetComponent<Outline>().effectColor = Color.red;
            return false;
        }
        return true;
    }

    bool ValidateAge()
    {
        if(String.IsNullOrEmpty(AgeDropdown.GetComponentInChildren<Text>().text))
        {
            AgeDropdown.GetComponent<Outline>().effectColor = Color.red;
            return false;
        }
        return true;
    }

    public void ResetIdOutline()
    {
        IdInputfield.GetComponent<Outline>().effectColor = Color.black;
        IdDropdown.GetComponent<Outline>().effectColor = Color.black;
    }

    public void ResetAgeOutline()
    {
        AgeDropdown.GetComponent<Outline>().effectColor = Color.black;
    }
}
