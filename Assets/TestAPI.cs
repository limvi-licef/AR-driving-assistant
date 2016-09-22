using HoloToolkit.Unity;
using System;
using UnityEngine;
using UnityEngine.UI;
//#if !UNITY_EDITOR
//using Windows.Perception;
//using Windows.Perception.Spatial;
//#endif

public class TestAPI : MonoBehaviour
{
    public Text orientationText;
    public Text positionText;
    public Text AngularAccelerationText;
    public Text LinearAccerationText;
    public Text AngularVelocityText;
    public Text LinearVelocityText;

//#if !UNITY_EDITOR
//    private SpatialLocator locator;
//    private SpatialLocation location;
//    private SpatialCoordinateSystem scs;

    void Start()
    {
        //locator = SpatialLocator.GetDefault();
        //scs = locator.CreateStationaryFrameOfReferenceAtCurrentLocation().CoordinateSystem;
        //location = locator.TryLocateAtTimestamp(PerceptionTimestampHelper.FromHistoricalTargetTime(DateTimeOffset.Now), scs);
        //InvokeRepeating("UpdateLocationTest", 4, 0.3F);
    }

    //void UpdateLocationTest()
    //{
    //    scs = locator.CreateStationaryFrameOfReferenceAtCurrentLocation().CoordinateSystem;
    //    location = locator.TryLocateAtTimestamp(PerceptionTimestampHelper.FromHistoricalTargetTime(DateTimeOffset.Now), scs);
    //    orientationText.text = location.Orientation.ToString();
    //    positionText.text = location.Position.ToString();
    //    AngularAccelerationText.text = location.AbsoluteAngularAcceleration.ToString();
    //    LinearAccerationText.text = location.AbsoluteLinearAcceleration.ToString();
    //    AngularVelocityText.text = location.AbsoluteAngularVelocity.ToString();
    //    LinearVelocityText.text = location.AbsoluteLinearVelocity.ToString();
    //}

    void Update()
    {
        orientationText.text = Camera.main.transform.eulerAngles.ToString();
        //positionText.text = location.Position.ToString();
        //AngularAccelerationText.text = Camera.main.transform.eulerAngles.ToString();
        //LinearAccerationText.text = location.AbsoluteLinearAcceleration.ToString();
        AngularVelocityText.text = Camera.main.velocity.ToString();
        //LinearVelocityText.text = location.AbsoluteLinearVelocity.ToString();
    }
    //#endif
}


