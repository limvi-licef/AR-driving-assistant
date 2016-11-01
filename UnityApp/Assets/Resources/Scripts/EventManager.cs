using UnityEngine;
using System.Collections.Generic;

public class EventManager : MonoBehaviour
{

    public delegate void TriggerAction(Event e);
    public static event TriggerAction OnTrigger;

    public delegate void TabClickAction();
    public static event TabClickAction OnTabClick;

    private List<Event> m_Events;

    void Start()
    {
        m_Events = new List<Event>();
        m_Events.Add(new Event() { Prefab = "AdviceDisplay", Sound = "WarningBeep", Color = new Color32(16,184,31,255), Icon = "info_icon", Text = "" });
        m_Events.Add(new Event() { Prefab = "AdviceDisplay", Sound = "WarningBeep", Color = Color.blue, Icon = "advice_icon", Text = "" });
        m_Events.Add(new Event() { Prefab = "LeftWarningDisplay", Sound = "WarningBeep", Color = Color.red, Icon = "warning_icon", Text = "" });
        m_Events.Add(new Event() { Prefab = "RightWarningDisplay", Sound = "WarningBeep", Color = Color.red, Icon = "warning_icon", Text = "" });
    }

    public static void SendEvent(Event e)
    {
        if (OnTrigger != null)
        {
            OnTabClick();
            OnTrigger(e);
        }
        
    }
}