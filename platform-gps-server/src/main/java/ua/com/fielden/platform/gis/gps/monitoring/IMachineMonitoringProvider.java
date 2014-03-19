package ua.com.fielden.platform.gis.gps.monitoring;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachineModuleTemporalAssociation;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AbstractAvlModule;

/**
 * A contract to provide access to machine related monitoring information that gets updated asynchronously at runtime during receiving of GPS messages.
 *
 * @author TG Team
 *
 */
public interface IMachineMonitoringProvider<
	MESSAGE extends AbstractAvlMessage,
	MACHINE extends AbstractAvlMachine<MESSAGE>,
	MODULE extends AbstractAvlModule,
	ASSOCIATION extends AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>
> {
    Map<Long, List<MESSAGE>> getLastMessagesUpdate(final Map<Long, Date> machinesTiming);

    /**
     * Every new association needs to be promoted to server cache to correctly handle volatile nature of modules for machine.
     * This API method does the job.
     */
    void promoteNewMachineAssociation(final ASSOCIATION machineModuleTemporalAssociation);

    /**
     * Every changed association needs to be promoted to server cache to correctly handle volatile nature of modules for machine.
     * This API method does the job.
     */
    void promoteChangedMachineAssociation(final ASSOCIATION machineModuleTemporalAssociation);

    /**
     * Every new machine needs to be promoted to server cache to correctly handle machine processing.
     * This API method does the job.
     */
    void promoteNewMachine(final MACHINE machine);

    /**
     * Every new module needs to be promoted to server cache to correctly handle machine processing.
     * This API method does the job.
     */
    void promoteNewModule(final MODULE module);

    /**
     * Every changed machine needs to be promoted to server cache to correctly handle machine processing.
     * This API method does the job.
     */
    void promoteChangedMachine(final MACHINE machine);
}
