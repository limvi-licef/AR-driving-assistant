using UnityEngine;

namespace HoloToolkit.Unity
{
    public partial class TapToPlaceUI : MonoBehaviour
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
                transform.position = (Camera.main.transform.position + Camera.main.transform.forward * DistanceToCamera);
                fixedAngle = Camera.main.transform.forward.y;
                gameObject.GetComponent<BoxCollider>().size = placingColliderSize;
            }
            else
            {
                gameObject.GetComponent<BoxCollider>().size = colliderSize;
                targetPosition = new Vector3(Camera.main.transform.forward.x, fixedAngle, Camera.main.transform.forward.z) * DistanceToCamera + Camera.main.transform.position;
                float distance = Vector3.Distance(UIComponent.GetComponent<RectTransform>().position, targetPosition) * scaleDifference;
                float width = UIComponent.GetComponent<RectTransform>().rect.width * widthScale;

                if (distance > width)
                {
                    transform.position = Vector3.SmoothDamp(transform.position, targetPosition, ref velocity, speed);
                }
            }
        }

    }
}
