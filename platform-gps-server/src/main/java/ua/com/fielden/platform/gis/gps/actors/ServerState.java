package ua.com.fielden.platform.gis.gps.actors;

import ua.com.fielden.platform.gis.gps.MachineServerState;

/**
 * A message type that contains response with a last server state.
 */
public class ServerState extends ServerStateResponse {
    private final Long machineId;
    private final MachineServerState serverState;

    public ServerState(final Long machineId, final MachineServerState serverState) {
        this.machineId = machineId;
        this.serverState = serverState;
    }

    public Long getMachineId() {
        return machineId;
    }

    public MachineServerState getServerState() {
        return serverState;
    }
}
