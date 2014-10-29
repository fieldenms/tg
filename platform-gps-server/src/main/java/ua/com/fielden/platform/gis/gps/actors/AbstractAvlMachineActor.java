package ua.com.fielden.platform.gis.gps.actors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.gis.MapUtils;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.utils.Pair;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * This actor is responsible for messages processing for concrete machine.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAvlMachineActor<MESSAGE extends AbstractAvlMessage, MACHINE extends AbstractAvlMachine<MESSAGE>> extends UntypedActor {
    private final MessagesComparator<MESSAGE> messagesComparator;
    protected static int jdbcInsertBatchSize = 100;
    private static int windowSize = 5;
    private static int windowSize2 = 24;
    private static int windowSize3 = 35;
    private final Logger logger = Logger.getLogger(AbstractAvlMachineActor.class);

    private MACHINE machine;
    private final LinkedList<Packet<MESSAGE>> incomingPackets = new LinkedList<>();
    private final LinkedList<Packet<MESSAGE>> inspectionBuffer = new LinkedList<>();
    private final Blackout<MESSAGE> blackout;
    private MESSAGE latestGpsMessage;
    private MESSAGE lastProcessedMessage;
    private final HibernateUtil hibUtil;
    private ActorRef machinesCounterRef;

    public AbstractAvlMachineActor(final EntityFactory factory, final MACHINE machine, final MESSAGE lastMessage, final HibernateUtil hibUtil, final ActorRef machinesCounterRef) {
	this.machinesCounterRef = machinesCounterRef;

	messagesComparator = new MessagesComparator<MESSAGE>();
	blackout = new Blackout<MESSAGE>(messagesComparator);

	this.machine = machine;
	this.latestGpsMessage = lastMessage;
	this.lastProcessedMessage = lastMessage;

	this.hibUtil = hibUtil;
	// do not forget to invoke processTempMessages()!
    }

    @Override
    public void preStart() {
        super.preStart();

        machinesCounterRef.tell(new MachineActorStarted(this.machine.getKey(), this.machine.getDesc()), getSelf());
        machinesCounterRef = null;
    }

    protected abstract void processTempMessages(final MACHINE machine) throws Exception;

    protected abstract void persistTemporarily(final Packet<MESSAGE> packet) throws Exception;

    protected abstract void persistError(final Packet<MESSAGE> packet) throws Exception;

    protected abstract void persist(final Collection<MESSAGE> messages, final MESSAGE latestPersistedMessage) throws Exception;

    private Pair<Packet<MESSAGE>, Packet<MESSAGE>> categoriseByViolations(final Packet<MESSAGE> packet, final MESSAGE lastProcesseMessage) {
	if (lastProcesseMessage == null) {
	    return new Pair<Packet<MESSAGE>, Packet<MESSAGE>>(packet, null);
	}

	final Packet<MESSAGE> goodPart = new Packet<MESSAGE>(new Date(packet.getCreated()), messagesComparator);
	final Packet<MESSAGE> badPart = new Packet<MESSAGE>(new Date(packet.getCreated()), messagesComparator);

	for (final MESSAGE message : packet.getMessages()) {
	    if (message.getGpsTime().getTime() <= lastProcesseMessage.getGpsTime().getTime()) {
		badPart.add(message);
	    } else {
		goodPart.add(message);
	    }
	}

	return new Pair<Packet<MESSAGE>, Packet<MESSAGE>>(goodPart, badPart);
    }

    protected final void processSinglePacket(final Packet<MESSAGE> originalPacket, final boolean onStart) throws Exception {
	final Pair<Packet<MESSAGE>, Packet<MESSAGE>> categorisedByViolations = categoriseByViolations(originalPacket, lastProcessedMessage);
	final Packet<MESSAGE> packetWithViolatingMessages = categorisedByViolations.getValue();
	if (packetWithViolatingMessages != null && !packetWithViolatingMessages.isEmpty()) {
	    persistError(packetWithViolatingMessages);
	}

	final Packet<MESSAGE> packet = categorisedByViolations.getKey();

	if (!packet.isEmpty()) {

	    if (latestGpsMessage != null && latestGpsMessage.getGpsTime().getTime() > packet.getStart().getGpsTime().getTime()) {
		for (final MESSAGE message : packet.getMessages()) {
		    if (message.getGpsTime().getTime() < latestGpsMessage.getGpsTime().getTime()) {
			message.setStatus(1);
		    }
		}
	    }

	    if (latestGpsMessage == null || latestGpsMessage.getGpsTime().getTime() < packet.getFinish().getGpsTime().getTime()) {
		final MESSAGE oldLatestGpsMessage = latestGpsMessage;
		latestGpsMessage = packet.getFinish();
		processLatestGpsMessage(oldLatestGpsMessage, latestGpsMessage);
	    }

	    if (!onStart) {
		persistTemporarily(packet);
	    }

	    incomingPackets.add(packet);
	    if (fillBuffer()) {
		final Packet<MESSAGE> first = inspectionBuffer.poll();

		if (blackout.getFinish() != null && blackout.getFinish().getGpsTime().getTime() >= first.getStart().getGpsTime().getTime()) {
		    blackout.add(first);
		} else {
		    final int maximumIndexOfPacketBreakingChronologyOfFirstPacket = findMaximumIndexOfPacketBreakingChronologyOfGivenPacket(first);
		    final boolean blackoutHappened = maximumIndexOfPacketBreakingChronologyOfFirstPacket != -1;
		    if (blackoutHappened) {
			blackout.add(first);
			moveFirstPacketsFromBufferIntoBlackout(maximumIndexOfPacketBreakingChronologyOfFirstPacket + 1);
		    } else {
			final int chronologyBreakingIndex = findMaximumIndexOfNeighboringChronologyBreakingPacket();
			final boolean validationSucceeded = chronologyBreakingIndex == -1;

			if (validationSucceeded) {
			    if (blackout.getMessages().size() > 0) {
				final MESSAGE blackoutLastMessage = blackout.getFinish();
				final MESSAGE blackoutStart = blackout.getStart();
				final Collection<MESSAGE> messages = blackout.reset();
				persist(messages, lastProcessedMessage);
				lastProcessedMessage = blackoutLastMessage;
			    }
			    persist(first.getMessages(), lastProcessedMessage);
			    lastProcessedMessage = first.getFinish();
			} else {
			    blackout.add(first);
			    moveFirstPacketsFromBufferIntoBlackout(chronologyBreakingIndex + 1);
			}
		    }
		}
	    }
	}
    }

    /**
     * Provides custom processing action after new 'latest GPS message' has been arrived.
     *
     * @param oldLatestGpsMessage
     * @param newLatestGpsMessage
     */
    protected void processLatestGpsMessage(final MESSAGE oldLatestGpsMessage, final MESSAGE newLatestGpsMessage) {
    }

    @Override
    public void onReceive(final Object data) throws Exception {
	try {
	    if (data instanceof Packet) {
		final Packet<MESSAGE> packet = (Packet<MESSAGE>) data;
		for (final MESSAGE message : packet.getMessages()) {
		    completeMessage(message);
		}
		processSinglePacket(packet, false);
	    } else if (data instanceof LastMessagesRequest) {
		final LastMessagesRequest lastMessageRequest = (LastMessagesRequest) data;
		// System.out.println("Запит про останнє повідомлення для машини " + machine + " після " + glm.getAfterDate() + ". Received: " + new Date() + " для актора " + getSelf());

		if (latestGpsMessage != null && (lastMessageRequest.getAfterDate() == null || latestGpsMessage.getGpsTime().getTime() > lastMessageRequest.getAfterDate().getTime())) {
		    final MESSAGE lastMessage = completeMessageCopy(produceIncompleteLastMessage(latestGpsMessage), latestGpsMessage);
		    getSender().tell(new LastMessage<MESSAGE>(lastMessageRequest.getMachineId(), lastMessage), getSelf());
		} else {
		    getSender().tell(new NoLastMessage(), getSelf());
		}
	    } else if (data instanceof Changed) {
		promoteChangedMachine((Changed<MACHINE>) data);
	    } else {
		unhandled(data);
	    }
	} catch (final Exception e) {
	    logger.error(e.getMessage(), e);
	    throw e;
	}
    }

    protected void promoteChangedMachine(final Changed<MACHINE> changedMachine) {
	setMachine(changedMachine.getValue());
	logger.info("An existent machine, that has been changed [" + changedMachine.getValue() + "], has been sucessfully promoted to its machine actor.");
    }

    private MESSAGE produceIncompleteLastMessage(final MESSAGE message) {
	final MESSAGE copy = createMessage();
	copy.setX(message.getX());
	copy.setY(message.getY());
	copy.setVectorSpeed(message.getVectorSpeed());
	copy.setVectorAngle(message.getVectorAngle());
	copy.setGpsTime(message.getGpsTime());
	copy.setTravelledDistance(message.getTravelledDistance());
	return copy;
    }

    protected final BigDecimal calcDistance(final MESSAGE prevMessage, final MESSAGE currMessage) {
	return new BigDecimal(MapUtils.calcDistance(prevMessage.getX(), //
		prevMessage.getY(), //
		currMessage.getX(), //
		currMessage.getY())).setScale(2, RoundingMode.HALF_UP);
    }

    private int findMaximumIndexOfPacketBreakingChronologyOfGivenPacket(final Packet<MESSAGE> firstPacket) {
	for (int i = inspectionBuffer.size() - 1; i >= 0; i--) {
	    final Packet<MESSAGE> currPacket = inspectionBuffer.get(i);
	    if (!(currPacket.getStart().getGpsTime().getTime() > firstPacket.getFinish().getGpsTime().getTime())) {
		return i;
	    }
	}
	return -1;
    }

    private int findMaximumIndexOfNeighboringChronologyBreakingPacket() {
	Date prevPacketStart = inspectionBuffer.get(inspectionBuffer.size() - 1).getStart().getGpsTime();
	for (int i = inspectionBuffer.size() - 2; i >= 0; i--) {
	    final Packet<MESSAGE> currPacket = inspectionBuffer.get(i);
	    if (!(currPacket.getFinish().getGpsTime().getTime() < prevPacketStart.getTime())) {
		return i + 1;
	    }
	    prevPacketStart = currPacket.getStart().getGpsTime();
	}
	return -1;
    }

    private void moveFirstPacketsFromBufferIntoBlackout(final int qty) {
	for (int index = 0; index < qty; index++) {
	    final Packet<MESSAGE> packet = inspectionBuffer.poll();
	    blackout.add(packet);
	}
    }

    private boolean fillBuffer() {
	int index = 1;
	final int limit = calcNewWindowSize(calcAvgPacketSize()) - inspectionBuffer.size();

	while (index <= limit) {
	    final Packet<MESSAGE> p = incomingPackets.poll();
	    if (p != null) {
		inspectionBuffer.add(p);
	    } else {
		return false;
	    }

	    index = index + 1;
	}
	return true;
    }

    private float calcAvgPacketSize() {
	if (inspectionBuffer.size() == 0) {
	    return 0;
	}

	Integer totalSize = 0;
	for (final Packet<MESSAGE> packet : inspectionBuffer) {
	    totalSize = totalSize + packet.getMessages().size();
	}
	return totalSize / inspectionBuffer.size();
    }

    private int calcNewWindowSize(final float recentAvgPacketSize) {
	if (recentAvgPacketSize < 1.1) {
	    return windowSize;
	} else if (recentAvgPacketSize < 1.3) {
	    return windowSize2;
	} else {
	    return windowSize3;
	}
    }

    /**
     * A method to create empty GPS message.
     *
     * @return
     */
    protected abstract MESSAGE createMessage();

    /**
     * A method to fill GPS message with client-specific data.
     *
     * @param message
     * @return
     */
    protected abstract MESSAGE completeMessage(final MESSAGE message);

    /**
     * A method to fill GPS message with client-specific data.
     *
     * @param populateData
     * @return
     */
    protected abstract MESSAGE completeMessageCopy(final MESSAGE populateData, final MESSAGE messageToCopyFrom);

    protected MACHINE getMachine() {
	return machine;
    }

    protected void setMachine(final MACHINE machine) {
	this.machine = machine;
    }

    protected HibernateUtil getHibUtil() {
	return hibUtil;
    }

    public MessagesComparator<MESSAGE> getMessagesComparator() {
	return messagesComparator;
    }
}