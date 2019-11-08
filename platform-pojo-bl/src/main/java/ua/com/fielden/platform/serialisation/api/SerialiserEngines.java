package ua.com.fielden.platform.serialisation.api;

/**
 * An enumeration that indicates the engine for serialisation. Currently only JACKSON is supported.
 *
 * @author TG Team
 *
 */
public enum SerialiserEngines {
    /**
     * Serialisation engine for HTML5/JS TG Client. This engine is used for web-browser TG client application, where the byte array deserialises to JavaScript objects (from JSON).
     */
    JACKSON
}
