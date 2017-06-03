using UnityEngine;
using System.Collections.Generic;

public class EventManager : MonoBehaviour
{

    public delegate void TriggerAction(Event e);
    public static event TriggerAction OnTrigger;

    /// <summary>
    /// Defines the Events to be shown to the user
    /// </summary>
    private static Dictionary<string, Event> m_Events;

    void Start()
    {
        m_Events = new Dictionary<string, Event>();
        m_Events[Config.EventTypes.INFORMATION] = new Event() { Prefab = "AdviceDisplay", Sound = "InformationBeep", Color = new Color32(16, 184, 31, 255), Icon = "info_icon", Text = "" };
        m_Events[Config.EventTypes.ADVICE] = new Event() { Prefab = "AdviceDisplay", Sound = "AdviceBeep", Color = Color.blue, Icon = "advice_icon", Text = "" };
        m_Events[Config.EventTypes.WARNING] = new Event() { Prefab = "AdviceDisplay", Sound = "WarningBeep", Color = Color.red, Icon = "warning_icon", Text = "" };
        m_Events[Config.EventTypes.LEFT_WARNING] = new Event() { Prefab = "LeftWarningDisplay", Sound = "WarningBeep", Color = Color.red, Icon = "warning_icon", Text = "" };
        m_Events[Config.EventTypes.RIGHT_WARNING] = new Event() { Prefab = "RightWarningDisplay", Sound = "WarningBeep", Color = Color.red, Icon = "warning_icon", Text = "" };
    }

    /// <summary>
    /// Fires a delegate action when a new event is received
    /// </summary>
    /// <param name="name">The name of the event to be shown</param>
    /// <param name="message">The message to be displayed during the event</param>
    public static void SendEvent(string name, string message)
    {
        Event e;
        if (OnTrigger != null && m_Events.TryGetValue(name, out e))
        {
            e.Text = message;
            OnTrigger(e);
        }
        
    }
}