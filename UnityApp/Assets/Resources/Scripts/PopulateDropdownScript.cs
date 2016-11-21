using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.UI;

/// <summary>
/// Script used to populate the id dropdown on the User panel
/// </summary>
public class PopulateDropdownScript : MonoBehaviour {

    public UDPSender UDPSender;
    public Text Placeholder;

    void Start ()
    {
        requestUserList();
    }

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
    }

    public void AddAndSelect (UserManager.User user)
    {
        var dropdown = GetComponent<Dropdown>();
        dropdown.options.Add(new Dropdown.OptionData(user.userName));
        dropdown.value = dropdown.options.Count - 1;
    }

    /// <summary>
    /// Sends a request for all Users
    /// </summary>
    private void requestUserList()
    {
        JsonClasses.JsonRequest idRequest = new JsonClasses.JsonRequest();
        idRequest.requestType = JsonClasses.UsersRequest;
        UDPSender.SendJSON(idRequest);
    }

}
