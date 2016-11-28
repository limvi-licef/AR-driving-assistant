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

    bool placing = false;
    float fixedAngle;
    Vector3 colliderSize;
    Vector3 placingColliderSize;
    Vector3 targetPosition;
    Vector3 velocity = Vector3.zero;

    void Start()
    {
        //Initialize a bigger collider to be used while placing ui to ensure the collider is always interactible no matter where the user places the ui
        colliderSize = gameObject.GetComponent<BoxCollider>().size;
        placingColliderSize = new Vector3(colliderSize.x * Config.HoloLensOnly.COLLIDER_SIZE_MULTIPLIER, colliderSize.y * Config.HoloLensOnly.COLLIDER_SIZE_MULTIPLIER, colliderSize.z);
        //Lock the y axis so the hologram doesn't block the road while driving
        fixedAngle = Camera.main.transform.forward.y;
    }

    void OnSelect()
    {
        placing = !placing;
    }

    void Update()
    {
        if(placing)
        {
            //Calculate the new position of the UI relative to the user
            transform.position = (Camera.main.transform.position + Camera.main.transform.forward * Config.HoloLensOnly.DISTANCE_TO_CAMERA);
            //Lock the UI to the center of the user's gaze
            fixedAngle = Camera.main.transform.forward.y;
            //Use bigger collider size
            gameObject.GetComponent<BoxCollider>().size = placingColliderSize;
        }
        else
        {
            //Reset default collider size
            gameObject.GetComponent<BoxCollider>().size = colliderSize;
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
