using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using System.Linq;

/// <summary>
/// Sends a request to the Android app to add a new user to the database
/// </summary>
public class NewUserForm : MonoBehaviour {

    private static readonly string defaultUser = "Utilisateur ";

    public UDPSender UDPSender;
    public Button sendButton;
    public InputField IdField;
    public Text AgeField;
    public ToggleGroup GenderRadio;
    public ToggleGroup AvatarRadio;

	void Start () {
        //Disable this gameobject until the user clicks on the new user button
        gameObject.SetActive(false);
        sendButton.onClick.AddListener(() => SendForm());
    }

    void OnEnable()
    {
        //HoloLens only : Auto generated user Id since there is no keyboard
        IdField.text = defaultUser + UserManager.UserCount;
    }

    /// <summary>
    /// Sends a new user insert request using the data in each fields
    /// </summary>
    public void SendForm()
    {
        JsonClasses.JsonRequestNewUser newUserRequest = new JsonClasses.JsonRequestNewUser();
        newUserRequest.requestType = JsonClasses.NewUserRequest;
        newUserRequest.userName = IdField.text;
        newUserRequest.userAge = int.Parse(AgeField.text);
        newUserRequest.userGender = GenderRadio.ActiveToggles().First().name;
        newUserRequest.userAvatar = int.Parse(AvatarRadio.ActiveToggles().First().name);
        UDPSender.SendJSON(newUserRequest);
    }

}
