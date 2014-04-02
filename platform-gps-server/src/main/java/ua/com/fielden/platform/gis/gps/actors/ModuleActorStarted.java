package ua.com.fielden.platform.gis.gps.actors;

/**
 * A message type that indicates that some module actor has been started.
 * 
 * @author TG Team
 * 
 */
public class ModuleActorStarted {
    private final String imei;

    public ModuleActorStarted(final String imei) {
        this.imei = imei;
    }

    public String getImei() {
        return imei;
    }
}
