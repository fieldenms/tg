package ua.com.fielden.platform.serialisation.api;

/**
 * An enumeration that indicates the engine for serialisation. Currently KRYO and JACKSON engines are supported.
 *
 * @author TG Team
 *
 */
public enum SerialiserEngines {
    /**
     * Serialisation engine for Java TG Client. This engine is used for Java TG client application, where the byte array deserialises back to Java objects.
     */
    KRYO,
    /**
     * Serialisation engine for HTML5/JS TG Client. This engine is used for web-browser TG client application, where the byte array deserialises to JavaScript objects (from JSON).
     */
    JACKSON
}
