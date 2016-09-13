using UnityEngine;
using System.Collections;
using UnityEngine.UI;

//TODO Link with actual trigger + trigger type info
public class EventManager : MonoBehaviour
{
    public delegate void TriggerAction(string prefabName);
    public static event TriggerAction OnTrigger;

    public delegate void TabClickAction();
    public static event TabClickAction OnTabClick;

    public void SendWarningEvent(string prefabName)
    {
        if (OnTrigger != null)
        {
            OnTabClick();
            OnTrigger(prefabName);
        }
        
    }

}