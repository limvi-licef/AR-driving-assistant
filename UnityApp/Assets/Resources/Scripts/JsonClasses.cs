using UnityEngine;
using System.Collections;
using System;
using System.Collections.Generic;

/// <summary>
/// Defines classes used to serialize and deserialize json
/// </summary>
/// <remarks>
/// UDPListenerThread.java receives and sends jsons formatted after these data structures
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
        public List<string> rides;
    }
}
