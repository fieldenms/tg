package ua.com.fielden.platform.gis.gps.actors;

import ua.com.fielden.platform.gis.gps.MachineServerState;

/**
 * A message type that contains request for last server state.
 */
public class LastServerStateRequest {
    private final Long machineId;
    private final MachineServerState oldServerState;

    public LastServerStateRequest(final Long machineId, final MachineServerState oldServerState) {
        this.machineId = machineId;
        this.oldServerState = oldServerState;
    }

    public Long getMachineId() {
        return machineId;
    }

    public MachineServerState getOldServerState() {
        return oldServerState;
    }
}
