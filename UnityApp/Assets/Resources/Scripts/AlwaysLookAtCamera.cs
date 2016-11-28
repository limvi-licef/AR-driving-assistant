using UnityEngine;
using System.Collections;

/// <summary>
/// Ensures the hologram is constantly facing the user
/// </summary>
/// <remarks>
/// For HoloLens
/// </remarks>
public class AlwaysLookAtCamera : MonoBehaviour {

    public Camera cam;

	void Update () {

        /*
         * Update the transform's rotation to always be oriented towards the camera
         * Quaternion.LookRotation creates a Quaternion rotation using the relative Vector3 rotation given to it
         * See https://docs.unity3d.com/ScriptReference/Quaternion.LookRotation.html
         */
        transform.rotation = Quaternion.LookRotation(transform.position - cam.transform.position);
    }
}
