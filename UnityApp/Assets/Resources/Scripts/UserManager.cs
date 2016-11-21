using UnityEngine;
using System.Collections.Generic;
using UnityEngine.UI;
using System;

/// <summary>
/// Holds methods to help manage the current user
/// </summary>
public class UserManager : MonoBehaviour {

    public PopulateDropdownScript userDropdown;
    public Text errorDisplay;
    public List<RawImage> avatars;
    public string userGender { get; set; }
    public string userId { get; set; }
    public int userAge { get; set; }
    public int userAvatar { get; set; }
    private List<User> users;
    public List<User> Users
    {
        get
        {
            return users;
        }
        set
        {
            users = value;
            userDropdown.Populate(users);
            UserCount = users.Count;
        }
    }

    public static int UserCount = 0;

    [Serializable]
    public class User
    {
        public string userGender;
        public string userName;
        public int userAge;
        public int userAvatar;
    }

    /// <summary>
    /// Set param user as current user
    /// </summary>
    /// <param name="user">The user to set</param>
    public void SetCurrentUser(User user)
    {
        userId = user.userName;
        userAge = user.userAge;
        userGender = user.userGender;
        userAvatar = user.userAvatar;
    }

    /// <summary>
    /// Adds a user to the dropdown and select it
    /// </summary>
    /// <param name="user">The user to add and select</param>
    public void AddNewUser(User user)
    {
        users.Add(user);
        userDropdown.AddAndSelect(user);
    }

    /// <summary>
    /// Displays an error message on UserPanel
    /// </summary>
    /// <param name="message"></param>
    public void DisplayError (string message)
    {
        errorDisplay.text = message;
    }

    /// <summary>
    /// Display the current user's avatar on each RawImage linked to this script
    /// </summary>
    public void DisplayAvatar()
    {
        foreach (RawImage a in avatars)
        {
            a.texture = Resources.Load("Images/avatar_" + userAvatar) as Texture;
        }
    }

    /// <summary>
    /// Find an user by name
    /// </summary>
    /// <param name="userName">The user id to search for</param>
    /// <returns>The requested user or null if not found</returns>
    public User FindUser(string userName)
    {
        foreach(User user in users)
        {
            if(user.userName.Equals(userName))
            {
                return user;
            }
        }
        return null;
    }
}
