package ua.com.fielden.platform.gis.gps.factory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachineModuleTemporalAssociation;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AbstractAvlModule;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlModuleActor;

import com.google.inject.Injector;

/**
 * An utility class to start GPS server services (like Netty GPS server and Machine actors).
 *
 * @author TG Team
 *
 */
public abstract class ApplicationConfigurationUtil<
	MESSAGE extends AbstractAvlMessage,
	MACHINE extends AbstractAvlMachine<MESSAGE>,
	MODULE extends AbstractAvlModule,
	ASSOCIATION extends AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>,
	MACHINE_ACTOR extends AbstractAvlMachineActor<MESSAGE, MACHINE>,
	MODULE_ACTOR extends AbstractAvlModuleActor<MESSAGE, MACHINE, MODULE, ASSOCIATION>
> {
    private final static Logger logger = Logger.getLogger(ApplicationConfigurationUtil.class);

    /** An utility class to start GPS server services (like Netty GPS server and Module + Machine actors). */
    public final void startGpsServices(final Properties props, final Injector injector) {
	// get all vehicles with their latest GPS message
	// the resultant map is used by both the GPS message handler for updating and CurrentMachinesState resource for read
	// thus a concurrent map is used to synchronize read/write operations

	// create and start all actors responsible for message handling
	final AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, MACHINE_ACTOR, MODULE_ACTOR> actors = createActors(fetchMachinesWithLastMessages(injector), fetchModulesWithAssociations(injector));
	actors.startActorSystem();

	promoteActors(injector, actors);
    }

    protected abstract void promoteActors(final Injector injector, final AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, MACHINE_ACTOR, MODULE_ACTOR> actors);

    /** Creates specific {@link AbstractActors} implementation. */
    protected abstract AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, MACHINE_ACTOR, MODULE_ACTOR> createActors(final Map<MACHINE, MESSAGE> machinesWithLastMessages, final Map<MODULE, List<ASSOCIATION>> modulesWithAssociations);

    /** Fetches all machines with their last messages to be used for processing. */
    protected abstract Map<MACHINE, MESSAGE> fetchMachinesWithLastMessages(final Injector injector);

    /** Fetches all modules with their associations to be used for processing. */
    protected abstract Map<MODULE, List<ASSOCIATION>> fetchModulesWithAssociations(final Injector injector);

    protected Logger getLogger() {
	return logger;
    }
}
