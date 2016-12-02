package com.limvi_licef.ar_driving_assistant.models;

/**
 * Data structure used to send User information to the UnityApp
 */
public class User {
    public String userName;
    public int userAge;
    public String userGender;
    public int userAvatar;

    public User(String userName, int userAge, String userGender, int userAvatar){
        this.userName = userName;
        this.userAge = userAge;
        this.userGender = userGender;
        this.userAvatar = userAvatar;
    }
}
