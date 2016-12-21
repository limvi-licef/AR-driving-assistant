using UnityEngine;
using System.Collections.Generic;
using UnityEngine.UI;

/// <summary>
/// Script used to populate the id dropdown on the User panel
/// </summary>
public class PopulateDropdownScript : MonoBehaviour {

    public UDPSender UDPSender;
    public Text Placeholder;

    /// <summary>
    /// Populates the dropdown using a list of Users
    /// </summary>
    /// <param name="users"></param>
    public void Populate(List<UserManager.User> users)
    {
        var dropdown = GetComponent<Dropdown>();
        dropdown.options.Clear();
        foreach (UserManager.User u in users)
        {
            dropdown.options.Add(new Dropdown.OptionData(u.userName));
        }
        if(users.Count > 0)
        {
            dropdown.RefreshShownValue();
            Placeholder.gameObject.SetActive(false);
        }
    }

    /// <summary>
    /// Add a user to the id dropdown and selects it
    /// </summary>
    /// <param name="user">The user to add</param>
    public void AddAndSelect (UserManager.User user)
    {
        var dropdown = GetComponent<Dropdown>();
        dropdown.options.Add(new Dropdown.OptionData(user.userName));
        dropdown.value = dropdown.options.Count - 1;
        dropdown.RefreshShownValue();
        Placeholder.gameObject.SetActive(false);
    }

    /// <summary>
    /// Sends a request to fetch all Users
    /// </summary>
    public void requestUserList()
    {
        JsonClasses.JsonRequest idRequest = new JsonClasses.JsonRequest();
        idRequest.requestType = Config.Communication.USERS_REQUEST;
        UDPSender.SendJSON(idRequest);
    }

}
