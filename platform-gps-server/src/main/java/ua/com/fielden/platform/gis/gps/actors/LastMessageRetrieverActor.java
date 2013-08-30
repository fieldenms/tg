package ua.com.fielden.platform.gis.gps.actors;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

/**
 * This actor retrieves last messages for a bunch of machines.
 *
 */
public class LastMessageRetrieverActor<T extends AbstractAvlMessage> extends UntypedActor {
    private final Logger logger = Logger.getLogger(LastMessageRetrieverActor.class);

    private final Map<Long, ActorRef> machineActors;
    private Integer machinesCount;
    private int receivedMachinesCount = 0;
    private final Map<Long, List<T>> lastMessages = new HashMap<>(600);
    private ActorRef originalRequester;

    public LastMessageRetrieverActor(final Map<Long, ActorRef> machineActors) {
	this.machineActors = machineActors;
    }

    /**
     * Creates machine actor under a "system" supervisor.
     *
     * @param system
     * @param hibUtil
     * @param machine
     * @return
     */
    protected static <T extends AbstractAvlMessage> ActorRef create(final ActorSystem system, final Map<Long, ActorRef> machineActors) {
	final ActorRef myActor = system.actorOf(new Props(new UntypedActorFactory() {
	    private static final long serialVersionUID = -6677642334839003771L;

	    public UntypedActor create() {
		return new LastMessageRetrieverActor<T>(machineActors);
	    }
	}), createName());
	return myActor;
    }

    /**
     * Creates a actor name using a {@link UUID#randomUUID()} uniqueness generator.
     *
     * @return
     */
    private static String createName() {
	return "actor_for_last_message_retrieval_" + UUID.randomUUID().toString();
    }

    @Override
    public void onReceive(final Object data) throws Exception {
	if (data instanceof MachinesTiming) {
	    logger.debug("Last messages request has been obtained for [" + getSelf() + "] actor.");
	    if (machinesCount != null) {
		unhandled("Multiple requests is not permitted for LastMessageRetrieverActor.");
	    }
	    originalRequester = getSender();
	    final MachinesTiming mt = (MachinesTiming) data;
	    machinesCount = 0;

	    for (final Entry<Long, Date> idAndDate : mt.getMachinesTiming().entrySet()) {
		if (machineActors.get(idAndDate.getKey()) != null) { // there are machines without GPS modules! No machine actor exists in this case
		    machinesCount++;
		    machineActors.get(idAndDate.getKey()).tell(new LastMessagesRequest(idAndDate.getValue()), getSelf());
		}
	    }
	} else if (data instanceof LastMessagesResponse) {
	    receivedMachinesCount++;
	    if (data instanceof LastMessages) {
		final LastMessages<T> lm = (LastMessages<T>) data;
		logger.debug("Non-empty last messages response has been obtained for [" + getSelf() + "] actor for machine id [" + lm.getMachineId() + "]. receivedMachinesCount == " + receivedMachinesCount + " (from " + machinesCount + ").");
		lastMessages.put(lm.getMachineId(), lm.getMessages());
	    } else if (data instanceof NoLastMessage) {
		logger.debug("Empty last messages response has been obtained for [" + getSelf() + "] actor. receivedMachinesCount == " + receivedMachinesCount + " (from " + machinesCount + ").");
		// no messages for this machine
	    } else {
		unhandled("Unsupported LastMessagesResponse type descendant.");
	    }

	    if (machinesCount > receivedMachinesCount) {
		// this is illegal situation!
		final String m = "The number of processed machines exceeds the number of requested machines.";
		logger.error(m);
		unhandled(m);
	    } else if (machinesCount.equals(receivedMachinesCount)) {
		logger.debug("Non-empty last messages response for all machines has been obtained for [" + getSelf() + "] actor.");

		// all machines have sent a response with a last message.
		// So a result has been accumulated => send it back to original requester
		originalRequester.tell(lastMessages, getSelf());

		// Stops this actor and all its supervised children
		getContext().stop(getSelf());
	    } else {
		unhandled(data);
	    }
	} else {
	    logger.error("Unrecognizable message (" + data + ") has been obtained.");
	    unhandled(data);
	}
    }
}
