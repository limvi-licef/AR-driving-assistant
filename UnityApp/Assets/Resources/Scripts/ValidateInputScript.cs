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

    /// <summary>
    /// Validates that the user id is valid and sets the current user to that user if so
    /// </summary>
    public void ValidateInput()
    {
        UserManager.User user = UserManager.FindUser(Label.text);

        if(user != null && !String.IsNullOrEmpty(user.userName))
        {
            UserManager.SetCurrentUser(user);
            UserManager.DisplayAvatar();
            UserManager.DisplayError(""); //clear message
            gameObject.SetActive(false);
            Menu.SetAsLastSibling();
        }
        else
        {
            UserManager.DisplayError(Config.ErrorMessages.INVALID_ID);
        }
    }
}
