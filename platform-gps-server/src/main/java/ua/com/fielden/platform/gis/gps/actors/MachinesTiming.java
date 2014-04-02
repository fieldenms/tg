package ua.com.fielden.platform.gis.gps.actors;

import java.util.Date;
import java.util.Map;

/**
 * A message type that contains actual request for this actor.
 */
public class MachinesTiming {
    private final Map<Long, Date> machinesTiming;

    public MachinesTiming(final Map<Long, Date> machinesTiming) {
        this.machinesTiming = machinesTiming;
    }

    public Map<Long, Date> getMachinesTiming() {
        return machinesTiming;
    }
}
