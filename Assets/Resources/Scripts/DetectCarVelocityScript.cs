using UnityEngine;
using System.Collections.Generic;
using System;
using UnityEngine.UI;

public class DetectCarVelocityScript : MonoBehaviour {

    public List<GameObject> disabledButtons;
    public Button homeButton;
    const float limit = 2f;

	void Update () {
        Vector3 velocity = gameObject.GetComponent<Camera>().velocity;
        if(Math.Abs(velocity.x) > limit || Math.Abs(velocity.y) > limit || Math.Abs(velocity.z) > limit)
        {
            foreach(GameObject go in disabledButtons)
            {
                go.GetComponent<Button>().interactable = false;
            }
            homeButton.onClick.Invoke();
        }
        else
        {
            foreach (GameObject go in disabledButtons)
            {
                go.GetComponent<Button>().interactable = true;
            }
        }
    }
}
