using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class VelocityTest : MonoBehaviour {

    public Camera cam;

	
	// Update is called once per frame
	void Update () {
        Vector3 a = cam.velocity;
        gameObject.GetComponent<Text>().text = a.ToString();
    }
}
