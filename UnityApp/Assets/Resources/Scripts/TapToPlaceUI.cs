using UnityEngine;

/// <summary>
/// Script that allows the user to move the UI 
/// </summary>
/// <remarks>
/// For HoloLens
/// </remarks>
public class TapToPlaceUI : MonoBehaviour
{
    const float scaleDifference = 500f;
    const float colliderScale = 10f;
    const float speed = 0.5f;
    const float widthScale = 1.5f;

    public GameObject UIComponent;
    public float DistanceToCamera = 2f;

    bool placing = false;
    float fixedAngle;
    Vector3 colliderSize;
    Vector3 placingColliderSize;
    Vector3 targetPosition;
    Vector3 velocity = Vector3.zero;

    void Start()
    {
        //Initialize a bigger collider to be used while placing ui
        colliderSize = gameObject.GetComponent<BoxCollider>().size;
        placingColliderSize = new Vector3(colliderSize.x * colliderScale, colliderSize.y * colliderScale, colliderSize.z);
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
            //Calculate the new relative position of the UI
            transform.position = (Camera.main.transform.position + Camera.main.transform.forward * DistanceToCamera);
            //Lock the UI to the user's gaze
            fixedAngle = Camera.main.transform.forward.y;
            gameObject.GetComponent<BoxCollider>().size = placingColliderSize;
        }
        else
        {
            //Set the new relative position of the UI
            gameObject.GetComponent<BoxCollider>().size = colliderSize;
            targetPosition = new Vector3(Camera.main.transform.forward.x, fixedAngle, Camera.main.transform.forward.z) * DistanceToCamera + Camera.main.transform.position;

            //Horizontally follow the user's gaze
            //Only move if the cursor is widthScale times away from UI so it remains interactible
            float distance = Vector3.Distance(UIComponent.GetComponent<RectTransform>().position, targetPosition) * scaleDifference;
            float interactibleWidth = UIComponent.GetComponent<RectTransform>().rect.height * widthScale;
            if (distance > interactibleWidth)
            {
                transform.position = Vector3.SmoothDamp(transform.position, targetPosition, ref velocity, speed);
            }
        }
    }

}
