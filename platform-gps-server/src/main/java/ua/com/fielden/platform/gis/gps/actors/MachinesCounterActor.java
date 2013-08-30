package ua.com.fielden.platform.gis.gps.actors;

import org.apache.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

/**
 * This actors counts started machine actors and provides necessary events after all actors have been started.
 *
 */
public class MachinesCounterActor extends UntypedActor {
    private final Logger logger = Logger.getLogger(MachinesCounterActor.class);

    private final int machinesCount;
    private final AbstractActors<?, ?, ?> actors;
    private int startedMachinesCount;

    public MachinesCounterActor(final int machinesCount, final AbstractActors<?, ?, ?> actors) {
	this.machinesCount = machinesCount;
	this.startedMachinesCount = 0;
	this.actors = actors;

	logger.info("\tMachine actors starting...");
    }

    /**
     * Creates an actor that counts started machine actors.
     *
     * @param system
     * @param machinesCount
     * @return
     */
    public static ActorRef create(final ActorSystem system, final int machinesCount, final AbstractActors<?, ?, ?> actors) {
	final ActorRef machinesCounterRef = system.actorOf(new Props(new UntypedActorFactory() {
	    private static final long serialVersionUID = -6677642334839003771L;

	    public UntypedActor create() {
		return new MachinesCounterActor(machinesCount, actors);
	    }
	}), "machines_counter_actor");
	return machinesCounterRef;
    }

    @Override
    public void onReceive(final Object data) throws Exception {
	if (data instanceof MachineActorStarted) {
	    startedMachinesCount++;

	    logger.info("\t\t" + startedMachinesCount + " / " + machinesCount);
	    if (startedMachinesCount > machinesCount) {
		// this is illegal situation!
		final String m = "The number of started machine actors exceeds the number of machines.";
		logger.error(m);
		unhandled(m);
	    } else if (startedMachinesCount == machinesCount) {
		logger.info("\tMachine actors started.");

		this.actors.startNettyGpsServer();

		// Stops this actor and all its supervised children
		getContext().stop(getSelf());
	    }
	} else {
	    logger.error("Unrecognizable message (" + data + ") has been obtained.");
	    unhandled(data);
	}
    }
}
