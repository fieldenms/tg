package ua.com.fielden.platform.gis.gps.actors;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AvlData;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.google.inject.Injector;


/**
 * A container for all actors that maintains messages.
 *
 * @author TG Team
 *
 */
public abstract class AbstractActors<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> {
    private final Logger logger = Logger.getLogger(AbstractActors.class);

    private final ActorSystem system;
    // an actors that represent machine processors, that contain last messages
    private final Map<Long, ActorRef> machineActors;

    /**
     * Creates an actor system responsible for processing messages and getting efficiently a state from it (e.g. last machine message).
     *
     * @param machines -- a current machines in a system
     *
     * TODO IMPORTANT: creating of a new machine is not supported yet in server runtime.
     *
     */
    public AbstractActors(final Injector injector, final Collection<M> machines) {
	this.system = ActorSystem.create("machine-actors");

	this.machineActors = new HashMap<>();
	for (final M machine : machines) {
	    this.machineActors.put(machine.getId(), create(injector, system, machine));
	}
    }

    /**
     * Creates machine actor under a "system" supervisor.
     *
     * @param system
     * @param hibUtil
     * @param machine
     * @return
     */
    protected final ActorRef create(final Injector injector, final ActorSystem system, final M machine) {
	final ActorRef myActor = system.actorOf(new Props(new UntypedActorFactory() {
	    private static final long serialVersionUID = -6677642334839003771L;

	    public UntypedActor create() {
		return createMachineActor(injector, machine);
	    }
	}), createName(machine));
	return myActor;
    }

    /**
     * Creates an instance of concrete {@link AbstractAvlMachineActor} implementation.
     *
     * @param injector
     * @param machine
     * @return
     */
    protected abstract N createMachineActor(final Injector injector, final M machine);

    /**
     * Creates a machine actor name using a transliterated version of machine's key.
     *
     * @param machine
     * @return
     */
    private final String createName(final M machine) {
	return "machine_" + Transliterator.transliterate(machine.getKey()).replaceAll(" ", "_").toLowerCase();
    }

    protected ActorRef getMachineActor(final Long machineId) {
	return machineActors.get(machineId);
    }

    protected ActorRef getMachineActor(final M machine) {
	return getMachineActor(machine.getId());
    }

    /**
     * An API method for handling received machine data.
     *
     * @param machine
     * @param data
     */
    public void dataReceived(final M machine, final AvlData[] data) {
	getMachineActor(machine).tell(data, null);
    }

    /**
     * An API method for handling received machine data.
     *
     * @param machine
     * @param data
     */
    public Map<Long, List<T>> getLastMessagesUpdate(final Map<Long, Date> machinesTiming) {
	final DateTime st = new DateTime();
	final Timeout timeout = new Timeout(Duration.create(50000, "seconds"));
	// TODO use several (or even one) existing LastMessageRetrieverActors? just not to create new ones every time
	final Future<Object> future = Patterns.ask(LastMessageRetrieverActor.create(system, machineActors), new MachinesTiming(machinesTiming), timeout);
	try {
	    final Map<Long, List<T>> result = (Map<Long, List<T>>) Await.result(future, timeout.duration());
	    final Period p = new Period(st, new DateTime());
	    logger.error("Last messages for " + machinesTiming.size() + " machines retrieved in " + (p.getHours() == 0 ? "" : p.getHours() + " h ") + (p.getMinutes() == 0 ? "" : p.getMinutes() + " m ") + p.getSeconds() + " s " + p.getMillis() + " ms");
	    return result;
	} catch (final Exception e) {
	    logger.error(e);
	    throw new IllegalStateException(e);
	}
    }
}
