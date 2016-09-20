using UnityEngine;

namespace HoloToolkit.Unity
{
    public partial class TapToPlaceUI : MonoBehaviour
    {
        public float distanceToCamera = 2f;
        bool placing = false;
        Vector3 colliderSize;
        Vector3 placingColliderSize;
        Vector3 localPosition;

        void Start()
        {
            colliderSize = gameObject.GetComponent<BoxCollider>().size;
            placingColliderSize = new Vector3(colliderSize.x * 10, colliderSize.y * 10, colliderSize.z);
            localPosition = (transform.position - Camera.main.transform.position).normalized * distanceToCamera;
        }

        void OnSelect()
        {
            placing = !placing;
        }

        void Update()
        {
            if(placing)
            {
                transform.position = Camera.main.transform.position + Camera.main.transform.forward * distanceToCamera;
                localPosition = (transform.position - Camera.main.transform.position).normalized * distanceToCamera;
                gameObject.GetComponent<BoxCollider>().size = placingColliderSize;
            }
            else
            {
                gameObject.GetComponent<BoxCollider>().size = colliderSize;
                transform.position = localPosition + Camera.main.transform.position;
            }
        }
    }
}
