using UnityEngine;
using System.Collections;
using System;
using System.Collections.Generic;

public class JsonClasses : MonoBehaviour {

    public static readonly string SpeedResponse = "speedCounter";
    public static readonly string EventResponse = "event";
    public static readonly string UsersResponse = "userList";
    public static readonly string NewUserResponse = "newUser";
    public static readonly string RidesResponse = "userRidesList";

    public static readonly string UsersRequest = "GetUsersList";
    public static readonly string NewUserRequest = "InsertNewUser";
    public static readonly string LastKnownRidesRequest = "GetLastKnownRides";

    [Serializable]
	public class JsonRequest
    {
        public string requestType;
    }

    [Serializable]
    public class JsonRequestNewUser : JsonRequest
    {
        public string userName;
        public int userAge;
        public string userGender;
        public int userAvatar;
    }

    [Serializable]
    public class JsonRequestLastKnownRides : JsonRequest
    {
        public string userId;
    }

    [Serializable]
    public class JsonResponse
    {
        public string requestType;
    }

    [Serializable]
    public class JsonResponseSpeed : JsonResponse
    {
        public string speedText;
    }

    [Serializable]
    public class JsonResponseEvent : JsonResponse
    {
        public string eventType;
        public string message;
    }

    [Serializable]
    public class JsonResponseUsers : JsonResponse
    {
        public List<UserManager.User> users;
    }

    [Serializable]
    public class JsonResponseInsert : JsonResponse
    {
        public bool status;
        public UserManager.User newUser;
    }

    [Serializable]
    public class JsonResponseLastKnown : JsonResponse
    {
        public List<string> rides;
    }
}
