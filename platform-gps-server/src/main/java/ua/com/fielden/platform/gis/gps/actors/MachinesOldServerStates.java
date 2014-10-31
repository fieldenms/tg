package ua.com.fielden.platform.gis.gps.actors;

import java.util.Map;

import ua.com.fielden.platform.gis.gps.MachineServerState;

/**
 * A message type that contains actual request for this actor.
 */
public class MachinesOldServerStates {
    private final Map<Long, MachineServerState> machinesServerStates;

    public MachinesOldServerStates(final Map<Long, MachineServerState> machinesServerStates) {
        this.machinesServerStates = machinesServerStates;
    }

    public Map<Long, MachineServerState> getMachinesServerStates() {
        return machinesServerStates;
    }
}
