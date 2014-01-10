package ua.com.fielden.platform.gis.gps.monitoring;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachineModuleTemporalAssociation;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AbstractAvlModule;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.Changed;
import ua.com.fielden.platform.gis.gps.actors.New;


/**
 * A default "carrier" implementation for {@link IMachineMonitoringProvider}.
 *
 * @author TG Team
 *
 */
public class DefaultMachineMonitoringProvider<
	MESSAGE extends AbstractAvlMessage,
	MACHINE extends AbstractAvlMachine<MESSAGE>,
	MODULE extends AbstractAvlModule,
	ASSOCIATION extends AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>
> implements IMachineMonitoringProvider<MESSAGE, MACHINE, MODULE, ASSOCIATION> {
    private final static Logger logger = Logger.getLogger(DefaultMachineMonitoringProvider.class);
    private AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, ?, ?> actors;

    public void setActors(final AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, ?, ?> actors) {
	this.actors = actors;
    }

    public AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, ?, ?> getActors() {
	return actors;
    }

    @Override
    public Map<Long, List<MESSAGE>> getLastMessagesUpdate(final Map<Long, Date> machinesTiming) {
        return actors.getLastMessagesUpdate(machinesTiming);
    }

    @Override
    public void promoteNewModule(final MODULE module) {
	actors.registerAndStartModuleActor(module, Arrays.<ASSOCIATION>asList());
	// FIXME please note that associations will not be retrieved for newly created module!
	// This has been done under assumption that no association will be appeared so quickly for new module.
    }

    @Override
    public void promoteNewMachine(final MACHINE machine) {
	if (actors != null) {
	    actors.registerAndStartMachineActor(machine, null);
	}

	// FIXME please note that last message will not be retrieved for newly created machine!
	// This has been done under assumption that no message will be appeared so quickly for new machine.
    }

    @Override
    public void promoteNewMachineAssociation(final ASSOCIATION machineModuleTemporalAssociation) {
        final MODULE module = machineModuleTemporalAssociation.getModule();
        actors.getModuleActor(module.getKey()).tell(new New<ASSOCIATION>(machineModuleTemporalAssociation), null);
        // return getResponseFromActor(actors.getModuleActor(module.getKey()), new New<ASSOCIATION>(machineModuleTemporalAssociation), 50000);
    }

    @Override
    public void promoteChangedMachineAssociation(final ASSOCIATION machineModuleTemporalAssociation) {
        final MODULE module = machineModuleTemporalAssociation.getModule();
        actors.getModuleActor(module.getKey()).tell(new Changed<ASSOCIATION>(machineModuleTemporalAssociation), null);
        // return getResponseFromActor(actors.getModuleActor(module.getKey()), new Changed<ASSOCIATION>(machineModuleTemporalAssociation), 50000);
    }
}
