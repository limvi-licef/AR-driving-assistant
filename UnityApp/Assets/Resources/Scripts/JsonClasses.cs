using UnityEngine;
using System.Collections;
using System;
using System.Collections.Generic;

/// <summary>
/// Defines classes used to serialize and deserialize json in order to communicate between the Unity app and the Android app
/// </summary>
/// <remarks>
/// TCPListenerAndroid.cs, TCPListenerHoloLens.cs and TCPListenerThread.java all receive and send jsons formatted after these data structures
/// </remarks>
public class JsonClasses : MonoBehaviour {

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
        public string status;
        public List<string> rides;
    }
}
