using UnityEngine;
using System.Collections;

public class FollowMainCamera : MonoBehaviour {

	void Update () {
        transform.position = Camera.main.transform.position;
	}
}
