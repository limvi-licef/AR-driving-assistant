using HoloToolkit.Unity;
using UnityEngine;
using UnityEngine.UI;
#if !UNITY_EDITOR
using Windows.Perception.Spatial;
#endif
namespace HoloToolkit.Unity
{
    public class TestAPI : MonoBehaviour
    {
#if !UNITY_EDITOR
        private SpatialLocation sl; 

        void Update()
        {
            gameObject.GetComponent<Text>().text = sl.orientation;
        }
#endif
    }
}

