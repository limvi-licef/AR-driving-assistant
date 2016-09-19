using UnityEngine;
using System.Collections;

public class AlwaysLookAtCamera : MonoBehaviour {

    public Camera cam;

	void Update () {
        transform.rotation = Quaternion.LookRotation(transform.position - cam.transform.position);
    }
}
