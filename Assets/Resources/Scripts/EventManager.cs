using UnityEngine;
using System.Collections;
using UnityEngine.UI;

//TODO Link with actual trigger + trigger type info
public class EventManager : MonoBehaviour
{
    public delegate void ClickAction(string prefabName);
    public static event ClickAction OnClicked;

    public void SendWarningEvent(string prefabName)
    {
        if (OnClicked != null)
        {
            OnClicked(prefabName);
        }
        
    }
}