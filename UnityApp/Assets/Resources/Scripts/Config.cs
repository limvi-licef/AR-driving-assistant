
/// <summary>
/// Regroups all constants used throughout the project in a single location
/// </summary>
public static class Config {

    /// <summary>
    /// Length in seconds that an event will be displayed
    /// </summary>
    public const float EVENT_DISPLAY_TIME = 5f;

    /// <summary>
    /// Title for the Retroaction results panel
    /// </summary>
    public const string LAST_KNOWN_RIDES_TITLE = "Derniers Trajets";

    /// <summary>
    /// Default app volume
    /// </summary>
    public const float DEFAULT_VOLUME = 0.5f;

    /// <summary>
    /// Defines the Event types
    /// Must match those in enum EventTypes in Events.java
    /// </summary>
    public static class EventTypes
    {
        public const string INFORMATION = "Information";
        public const string ADVICE = "Advice";
        public const string WARNING = "Warning";
        public const string LEFT_WARNING = "LeftWarning";
        public const string RIGHT_WARNING = "RightWarning";
    }

    /// <summary>
    /// Constants needed to communicate with the Android app
    /// </summary>
    public static class Communication
    {
        /// <summary>
        /// Port number this app listen on and sends to
        /// </summary>
        public const int PORT = 12345;

        /// <summary>
        /// The default ip address of a Android hotspot host
        /// </summary>
        public const string DEFAULT_IP = "192.168.43.1";

        /// <summary>
        /// Values defining the different types of requests and responses between the two apps
        /// The values must match those in Config.java in HoloLens class
        /// </summary>
        public const string SPEED_RESPONSE = "speedCounter";
        public const string EVENT_RESPONSE = "event";
        public const string USERS_RESPONSE = "userList";
        public const string NEW_USER_RESPONSE = "newUser";
        public const string RIDES_RESPONSE = "userRidesList";
        public const string USERS_REQUEST = "GetUsersList";
        public const string NEW_USER_REQUEST = "InsertNewUser";
        public const string LAST_KNOWN_RIDES_REQUEST = "GetLastKnownRides";
    }

    /// <summary>
    /// Defines error messages to be displayed to the user
    /// </summary>
    public static class ErrorMessages
    {
        /// <summary>
        /// Displayed if a new user could not be inserted into the Android database for any reason
        /// </summary>
        public const string NEW_USER_FAILURE = "Impossible de créer un nouvel utilisateur";

        /// <summary>
        /// Displayed if the chosen ID in the ID dropdown is invalid
        /// </summary>
        public const string INVALID_ID = "ID invalide";
    }

    /// <summary>
    /// Defines constants only used by the HoloLens
    /// </summary>
    public static class HoloLensOnly
    {
        /// <summary>
        /// Auto generated id prefix when adding a new user since there is no keyboard available
        /// Current user count will determine the number to be added
        /// eg. "Utilisateur 3"
        /// </summary>
        public const string DEFAULT_USER_PREFIX = "Utilisateur ";

        /// <summary>
        /// Defines the difference in scale between world space and UI size
        /// Shouldn't need to be changed
        /// </summary>
        public const float UI_SCALE_DIFFERENCE = 500f;

        /// <summary>
        /// Multiplier applied to collider size while moving the hologram
        /// Allows the tap gesture to be recognized no matter where the hologram is placed
        /// </summary>
        public const float COLLIDER_SIZE_MULTIPLIER = 10f;

        /// <summary>
        /// The movement speed of the hologram
        /// </summary>
        public const float HOLOGRAM_SPEED = 0.5f;

        /// <summary>
        /// Multiplier applied to hologram width to determine the interactible zone
        /// Gazing beyond this will make the hologram follow your gaze
        /// </summary>
        public const float WIDTH_SCALE_MULTIPLIER = 1.5f;

        /// <summary>
        /// Distance of the hologram from the user
        /// </summary>
        public const float DISTANCE_TO_CAMERA = 2f;
    }
}
