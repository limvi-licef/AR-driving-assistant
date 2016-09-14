using UnityEngine;
using System.Collections;

public class UserManager : MonoBehaviour {

    string userId;
    int userAge;

	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
	
	}

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
}
