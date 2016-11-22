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
        transform.rotation = Quaternion.LookRotation(transform.position - cam.transform.position);
    }
}
