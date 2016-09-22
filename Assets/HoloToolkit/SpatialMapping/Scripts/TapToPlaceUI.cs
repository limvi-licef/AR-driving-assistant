using UnityEngine;

namespace HoloToolkit.Unity
{
    public partial class TapToPlaceUI : MonoBehaviour
    {
        const float scaleDifference = 500;
        public GameObject UIComponent;
        public float DistanceToCamera = 2f;
        bool placing = false;
        Vector3 colliderSize;
        Vector3 placingColliderSize;
        Vector3 newPosition = Vector3.zero;
        Vector3 targetPosition;
        float fixedAngle;

        void Start()
        {
            colliderSize = gameObject.GetComponent<BoxCollider>().size;
            placingColliderSize = new Vector3(colliderSize.x * 10, colliderSize.y * 10, colliderSize.z);
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
                //transform.position = Vector3.MoveTowards(transform.position, targetPosition, Time.deltaTime);
                float distance = Vector3.Distance(UIComponent.GetComponent<RectTransform>().position, targetPosition) * scaleDifference;
                float width = UIComponent.GetComponent<RectTransform>().rect.width;

                if (distance > width * 1.5)
                {
                    transform.position = Vector3.Slerp(transform.position, targetPosition, Time.deltaTime);
                    //Vector3 lerpTargetPosition = targetPosition;
                    //lerpTargetPosition = Vector3.Lerp(transform.position, lerpTargetPosition, 1f);
                    //newPosition = Interpolator.NonLinearInterpolateTo(transform.position, lerpTargetPosition, Time.deltaTime, 9.8f);
                    //if ((targetPosition - newPosition).sqrMagnitude <= 0.0000001f)
                    //{
                    //    newPosition = targetPosition;
                    //}
                    //transform.position = newPosition;
                }
            }
        }

    }
}
