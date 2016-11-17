using UnityEngine;
using System.Collections.Generic;
using UnityEngine.UI;

public class UserManager : MonoBehaviour {

    public List<RawImage> avatars;
    string userGender;
    string userId;
    int userAge;
    int userAvatar;

    public string GetUserId()
    {
        return this.userId;
    }

    public void SetUserId(string id)
    {
        this.userId = id;
    }

    public int GetUserAge()
    {
        return this.userAge;
    }

    public void SetUserAge(int age)
    {
        this.userAge = age;
    }

    public string GetUserGender()
    {
        return this.userGender;
    }

    public void SetUserGender(string gender)
    {
        this.userGender = gender;
    }

    public int GetUserAvatar()
    {
        return this.userAvatar;
    }

    public void SetUserAvatar(int avatar)
    {
        this.userAvatar = avatar;
    }

    public void DisplayAvatar()
    {
        foreach (RawImage a in avatars)
        {
            a.texture = Resources.Load("Images/avatar_" + userAvatar) as Texture;
        }
    }
}
