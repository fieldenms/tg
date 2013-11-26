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
    private final AbstractActors<?, ?, ?, ?, ?, ?> actors;
    private int startedMachinesCount;

    public MachinesCounterActor(final int machinesCount, final AbstractActors<?, ?, ?, ?, ?, ?> actors) {
	this.machinesCount = machinesCount;
	this.startedMachinesCount = 0;
	this.actors = actors;
    }

    /**
     * Creates an actor that counts started machine actors.
     *
     * @param system
     * @param machinesCount
     * @return
     */
    public static ActorRef create(final ActorSystem system, final int machinesCount, final AbstractActors<?, ?, ?, ?, ?, ?> actors) {
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
	    final MachineActorStarted info = (MachineActorStarted) data;
	    startedMachinesCount++;

	    logger.info("\t\t" + startedMachinesCount + " / " + machinesCount + " [" + info.getKey() + " -- " + info.getDesc() + "]");
	    if (startedMachinesCount > machinesCount) {
		logger.info("\t\t[Additional machine actor created after registration of new machine].");
	    } else if (startedMachinesCount == machinesCount) {
		logger.info("\tMachine actors started.");

		this.actors.machineActorsStartedPostAction();
	    }
	} else {
	    logger.error("Unrecognizable message (" + data + ") has been obtained.");
	    unhandled(data);
	}
    }
}
