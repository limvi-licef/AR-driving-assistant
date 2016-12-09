using UnityEngine;

/// <summary>
/// Script that allows the user to move the UI 
/// </summary>
/// <remarks>
/// For HoloLens
/// </remarks>
public class TapToPlaceUI : MonoBehaviour
{
    public GameObject UIComponent;
    public GameObject MovementButton;

    bool placing = false;
    float fixedAngle;
    Vector3 defaultScale;
    Vector3 placingScale;
    Vector3 targetPosition;
    Vector3 velocity = Vector3.zero;

    /// <summary>
    /// Toggles whether the UI is in placing mode or not
    /// When toggled on, the UI will follow the user's gaze until a tap gesture is detected
    /// </summary>
    public void ToggleMovement()
    {
        placing = !placing;

        /// When placing, make the button size large enough to cover the whole UI
        /// Hack to ensure the tap gesture is detected no matter where the hologram is placed
        if (placing)
        {
            MovementButton.transform.localScale = placingScale;
        }
        else
        {
            MovementButton.transform.localScale = defaultScale;
        }
    }

    void Start()
    {
        // Save default button scale and define a bigger scale
        defaultScale = MovementButton.transform.localScale;
        placingScale = defaultScale * Config.HoloLensOnly.COLLIDER_SIZE_MULTIPLIER;

        //Lock the y axis so the hologram doesn't block the road while driving
        fixedAngle = Camera.main.transform.forward.y;
    }

    void Update()
    {
        if(placing)
        {
            //Calculate the new position of the UI relative to the user
            transform.position = (Camera.main.transform.position + Camera.main.transform.forward * Config.HoloLensOnly.DISTANCE_TO_CAMERA);
            //Lock the UI to the center of the user's gaze
            fixedAngle = Camera.main.transform.forward.y;
        }
        else
        {
            //Set the new relative position of the UI compared to the user
            targetPosition = new Vector3(Camera.main.transform.forward.x, fixedAngle, Camera.main.transform.forward.z) * Config.HoloLensOnly.DISTANCE_TO_CAMERA + Camera.main.transform.position;

            //Calculate the distance between the UI and the user's gaze
            float distance = Vector3.Distance(UIComponent.GetComponent<RectTransform>().position, targetPosition) * Config.HoloLensOnly.UI_SCALE_DIFFERENCE;
            //Using height since UIComponent has been rotated 90 degrees
            float interactibleWidth = UIComponent.GetComponent<RectTransform>().rect.height * Config.HoloLensOnly.WIDTH_SCALE_MULTIPLIER;

            //Only move if the cursor is widthScale times away from UI so it remains interactible
            if (distance > interactibleWidth)
            {
                //Make the hologram smoothly horizontally follow the user's gaze
                transform.position = Vector3.SmoothDamp(transform.position, targetPosition, ref velocity, Config.HoloLensOnly.HOLOGRAM_SPEED);
            }
        }
    }

}
