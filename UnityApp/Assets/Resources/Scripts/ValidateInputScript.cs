using UnityEngine;
using UnityEngine.UI;
using System;

public class ValidateInputScript : MonoBehaviour {

    public Dropdown IdDropdown;
    public Text Label;
    public Text Placeholder;
    public UserManager UserManager;
    public Transform Menu;

    void Start()
    {
        gameObject.SetActive(true);
    }

    public void ValidateInput()
    {
        if(ValidateID())
        {
            UserManager.SetUserId(Label.text);
            UserManager.SetUserAge(67);
            UserManager.SetUserGender("Homme");
            UserManager.SetUserAvatar(0);
            UserManager.DisplayAvatar();
            gameObject.SetActive(false);
            Menu.SetAsLastSibling();
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
