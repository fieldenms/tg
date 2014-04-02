package ua.com.fielden.platform.gis.gps.actors;

/**
 * A message type that indicates that some machine actor has been started.
 * 
 * @author TG Team
 * 
 */
public class MachineActorStarted {
    private final String key;
    private final String desc;

    public MachineActorStarted(final String key, final String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }
}
