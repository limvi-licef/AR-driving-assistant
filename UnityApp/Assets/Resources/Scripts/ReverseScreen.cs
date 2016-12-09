using System.Collections;
using System.Collections.Generic;
using UnityEngine;

/// <summary>
/// From http://answers.unity3d.com/questions/353680/mirror-flip-camera.html
/// </summary>
public class ReverseScreen : MonoBehaviour {

    public Camera Camera;

    /// <summary>
    /// Flips the screen on the X axis
    /// </summary>
	public void toggleReverse()
    {
        Camera.projectionMatrix = Camera.projectionMatrix * Matrix4x4.Scale(new Vector3(-1, 1, 1));
    }
}
