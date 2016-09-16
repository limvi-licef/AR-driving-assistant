using UnityEngine;
using System.Collections;

public class AlwaysLookAtCamera : MonoBehaviour {

    public Camera cam;
    bool movingUI = false;

	void Update () {
        transform.rotation = Quaternion.LookRotation(transform.position - cam.transform.position);
        transform.position = movingUI ? cam.transform.position + cam.transform.forward * 2 : transform.position;
    }

    public void MoveUI()
    {
        movingUI = !movingUI;
    }
}
