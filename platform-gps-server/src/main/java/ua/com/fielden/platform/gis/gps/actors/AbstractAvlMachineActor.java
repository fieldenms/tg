package ua.com.fielden.platform.gis.gps.actors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.gis.MapUtils;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.persistence.HibernateUtil;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * This actor is responsible for messages processing for concrete machine.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAvlMachineActor<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>> extends UntypedActor {
    private final MessagesComparator<T> messagesComparator;
    protected static int jdbcInsertBatchSize = 100;
    private static int windowSize = 5;
    private static int windowSize2 = 10;
    private static int windowSize3 = 30;
    private final Logger logger = Logger.getLogger(AbstractAvlMachineActor.class);
    private static BigDecimal TWO = new BigDecimal(2);
    private static BigDecimal TWO_MINUS = new BigDecimal(-2);

    private final M machine;
    private final LinkedList<Packet<T>> incomingPackets = new LinkedList<>();
    private final LinkedList<Packet<T>> inspectionBuffer = new LinkedList<>();
    private final Blackout<T> blackout;
    private T latestGpsMessage;
    private T lastProcessedMessage;
    private final HibernateUtil hibUtil;
    private final AvlToMessageConverter<T> avlToMessageConverter = new AvlToMessageConverter<>();
    private ActorRef machinesCounterRef;

    public AbstractAvlMachineActor(final EntityFactory factory, final M machine, final T lastMessage, final HibernateUtil hibUtil, final ActorRef machinesCounterRef) {
	this.machinesCounterRef = machinesCounterRef;

	messagesComparator = new MessagesComparator<T>();
	blackout = new Blackout<T>(messagesComparator);
	this.machine = machine;

	this.latestGpsMessage = lastMessage;
	this.lastProcessedMessage = lastMessage;

	final Properties props = new Properties();
	props.setProperty("user", "postgres");

	this.hibUtil = hibUtil;
	// do not forget to invoke processTempMessages()!
    }

    @Override
    public void preStart() {
        super.preStart();

        machinesCounterRef.tell(new MachineActorStarted(), getSelf());
        machinesCounterRef = null;
    }

    protected abstract void processTempMessages(final M machine) throws Exception;

    protected abstract void persistTemporarily(final Packet<T> packet) throws Exception;

    protected abstract void persistError(final Packet<T> packet) throws Exception;

    protected abstract void persist(final Collection<T> messages, final T first, final T last, final T latestPersistedMessage) throws Exception;

    protected final void processSinglePacket(final Packet<T> packet, final boolean onStart) throws Exception {
	if (!packet.isEmpty()) {
	    if (latestGpsMessage == null || latestGpsMessage.getGpsTime().getTime() < packet.getFinish().getGpsTime().getTime()) {
		latestGpsMessage = packet.getFinish();
	    }

	    if (!onStart) {
		persistTemporarily(packet);
	    }

	    incomingPackets.add(packet);
	    if (fillBuffer()) {
		final Packet<T> first = inspectionBuffer.poll();

		if (lastProcessedMessage != null && lastProcessedMessage.getGpsTime().getTime() >= first.getStart().getGpsTime().getTime()) {
		    persistError(first);
		} else if (blackout.getFinish() != null && blackout.getFinish().getGpsTime().getTime() >= first.getStart().getGpsTime().getTime()) {
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
				final T blackoutLastMessage = blackout.getFinish();
				final T blackoutStart = blackout.getStart();
				final Collection<T> messages = blackout.reset();
				persist(messages, blackoutStart, blackoutLastMessage, lastProcessedMessage);
				lastProcessedMessage = blackoutLastMessage;
			    }
			    persist(first.getMessages(), first.getStart(), first.getFinish(), lastProcessedMessage);
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

    @Override
    public void onReceive(final Object data) throws Exception {
	if (data instanceof AvlData[]) {
	    processSinglePacket(createPacket((AvlData[]) data), false);
	} else if (data instanceof Packet) {
	    final Packet<T> packet = (Packet<T>) data;
	    for (final T message : packet.getMessages()) {
		completeMessage(message);
	    }
	    processSinglePacket(packet, false);
	} else if (data instanceof LastMessagesRequest) {
	    final LastMessagesRequest glm = (LastMessagesRequest) data;
	    // System.out.println("Запит про останнє повідомлення для машини " + machine + " після " + glm.getAfterDate() + ". Received: " + new Date() + " для актора " + getSelf());

	    if (latestGpsMessage != null) {
		final List<T> lastMessages = new ArrayList<>();

		if (glm.isOnlyOne()) { // only single last message is needed
		    if (glm.getAfterDate() == null || latestGpsMessage.getGpsTime().getTime() > glm.getAfterDate().getTime()) {
			lastMessages.add(completeMessageCopy(produceIncompleteLastMessage(machine, latestGpsMessage), latestGpsMessage));
		    }
		} else {
		    if (glm.getAfterDate() == null || latestGpsMessage.getGpsTime().getTime() > glm.getAfterDate().getTime()) {
			lastMessages.add(completeMessageCopy(produceIncompleteLastMessage(machine, latestGpsMessage), latestGpsMessage));
		    }
		    // TODO does not supported, maybe will be deprecated at all
		    //		    MessageWithMarkers current = messageQueue.last();
		    //		    while (lastMessages.size() <= MachineMonitor.MAX_LAST_MESSAGES_COUNT && current != null && current.message().getGpsTime().getTime() > glm.getAfterDate().getTime()) {
		    //			lastMessages.add(produceLastMessage(machine, current.message()));
		    //			current = messageQueue.lower(current);
		    //		    }
		    //		    Collections.reverse(lastMessages);
		}

		if (lastMessages.isEmpty()) {
		    getSender().tell(new NoLastMessage(), getSelf());
		} else {
		    getSender().tell(new LastMessages<T>(machine.getId(), lastMessages), getSelf());
		}
	    } else {
		getSender().tell(new NoLastMessage(), getSelf());
	    }
	} else {
	    unhandled(data);
	}
    }

    private T produceIncompleteLastMessage(final M machine, final T message) {
	final T copy = createMessage();
	copy.setX(message.getX());
	copy.setY(message.getY());
	copy.setVectorSpeed(message.getVectorSpeed());
	copy.setVectorAngle(message.getVectorAngle());
	copy.setGpsTime(message.getGpsTime());
	copy.setTravelledDistance(message.getTravelledDistance());
	return copy;
    }

    protected final BigDecimal calcDistance(final T prevMessage, final T currMessage) {
	return new BigDecimal(MapUtils.calcDistance(prevMessage.getX(), //
		prevMessage.getY(), //
		currMessage.getX(), //
		currMessage.getY())).setScale(2, RoundingMode.HALF_UP);
    }

    private int findMaximumIndexOfPacketBreakingChronologyOfGivenPacket(final Packet<T> firstPacket) {
	for (int i = inspectionBuffer.size() - 1; i >= 0; i--) {
	    final Packet<T> currPacket = inspectionBuffer.get(i);
	    if (!(currPacket.getStart().getGpsTime().getTime() > firstPacket.getFinish().getGpsTime().getTime())) {
		return i;
	    }
	}
	return -1;
    }

    private int findMaximumIndexOfNeighboringChronologyBreakingPacket() {
	Date prevPacketStart = inspectionBuffer.get(inspectionBuffer.size() - 1).getStart().getGpsTime();
	for (int i = inspectionBuffer.size() - 2; i >= 0; i--) {
	    final Packet<T> currPacket = inspectionBuffer.get(i);
	    if (!(currPacket.getFinish().getGpsTime().getTime() < prevPacketStart.getTime())) {
		return i + 1;
	    }
	    prevPacketStart = currPacket.getStart().getGpsTime();
	}
	return -1;
    }

    private void moveFirstPacketsFromBufferIntoBlackout(final int qty) {
	for (int index = 0; index < qty; index++) {
	    final Packet<T> packet = inspectionBuffer.poll();
	    blackout.add(packet);
	}
    }

    private boolean fillBuffer() {
	int index = 1;
	final int limit = calcNewWindowSize(calcAvgPacketSize()) - inspectionBuffer.size();

	while (index <= limit) {
	    final Packet<T> p = incomingPackets.poll();
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
	for (final Packet<T> packet : inspectionBuffer) {
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

    private Packet<T> createPacket(final AvlData[] data) {
	final Date packetReceived = new Date();
	final Packet<T> packet = new Packet<T>(packetReceived, messagesComparator);
	for (int i = data.length - 1; i >= 0; i--) {
	    final T msg = completeMessage(avlToMessageConverter.populateData(createMessage(), data[i], packetReceived)); // AvlToMessageConverter.convert(data[i], machine, machineRouteDriverHistoryMaintainer, packetReceived);

	    if (isValid(msg)) {
		packet.add(msg);
	    }
	}
	return packet;
    }

    /**
     * A method to create empty GPS message.
     *
     * @return
     */
    protected abstract T createMessage();

    /**
     * A method to fill GPS message with client-specific data.
     *
     * @param message
     * @return
     */
    protected abstract T completeMessage(T message);

    /**
     * A method to fill GPS message with client-specific data.
     *
     * @param populateData
     * @return
     */
    protected abstract T completeMessageCopy(T populateData, final T messageToCopyFrom);

    private boolean isZero(final BigDecimal value) {
	return BigDecimal.ZERO.compareTo(value) == 0;
    }

    private boolean isNearZero(final BigDecimal value) {
	return TWO_MINUS.compareTo(value) < 0 && TWO.compareTo(value) > 0;
    }

    private boolean isValid(final T msg) {
	if (isZero(msg.getX()) || isZero(msg.getY()) || (isNearZero(msg.getX()) && isNearZero(msg.getY()))) {
	    return false;
	} else if ((msg.getGpsTime().getTime() - (new Date()).getTime()) > 600000)/*10 minutes*/{
	    return false;
	} else if (msg.getVectorSpeed().equals(255)) {
	    return false;
	}

	return true;
    }

    protected M getMachine() {
	return machine;
    }

    protected HibernateUtil getHibUtil() {
	return hibUtil;
    }

    public MessagesComparator<T> getMessagesComparator() {
	return messagesComparator;
    }
}