package ua.com.fielden.platform.gis.gps.actors;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachineModuleTemporalAssociation;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AbstractAvlModule;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.Option;
import ua.com.fielden.platform.persistence.HibernateUtil;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * This actor is responsible for messages processing for concrete module and redirection to appropriate machine actors.
 * <p>
 * Please note, that this actor should consult with associations actor to determine active association for every message arrived.
 * There can be also situations when no vehicle is associated with the module. In this case messages can be disregarded or perhaps saved in some other storage
 * to distinguish from 'normal' ones.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAvlModuleActor<
	MESSAGE extends AbstractAvlMessage,
	MACHINE extends AbstractAvlMachine<MESSAGE>,
	MODULE extends AbstractAvlModule,
	ASSOCIATION extends AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>
	> extends UntypedActor {

    private final static Logger logger = Logger.getLogger(AbstractAvlModuleActor.class);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

    private final MessagesComparator<MESSAGE> messagesComparator;
    private final Comparator<ASSOCIATION> machineAssociationsComparator;
    private final MODULE module;
    private final HibernateUtil hibUtil;
    private final AvlToMessageConverter<MESSAGE> avlToMessageConverter = new AvlToMessageConverter<>();
    private ActorRef modulesCounterRef;
    private final List<ASSOCIATION> machineAssociations;
    private final AbstractActors<?, ?, ?, ?, ?, ?> actors;

    public AbstractAvlModuleActor(final EntityFactory factory, final MODULE module, final List<ASSOCIATION> machineAssociations, final HibernateUtil hibUtil, final ActorRef modulesCounterRef, final AbstractActors<?, ?, ?, ?,?, ?> actors) {
	this.modulesCounterRef = modulesCounterRef;
	this.actors = actors;
	this.module = module;

	messagesComparator = new MessagesComparator<MESSAGE>();
	machineAssociationsComparator = new Comparator<ASSOCIATION>() {
	    @Override
	    public int compare(final ASSOCIATION association1, final ASSOCIATION association2) {
		return association1.getFrom().compareTo(association2.getFrom());
	    }
	};

	this.machineAssociations = machineAssociations;
	Collections.sort(this.machineAssociations, machineAssociationsComparator);

	final Properties props = new Properties();
	props.setProperty("user", "postgres");

	this.hibUtil = hibUtil;
    }

    @Override
    public void preStart() {
        super.preStart();

        modulesCounterRef.tell(new ModuleActorStarted(this.module.getKey()), getSelf());
        modulesCounterRef = null;
    }

    @Override
    public void onReceive(final Object data) throws Exception {
	try {
	    if (data instanceof AvlData[]) {
		final Packet<MESSAGE> packet = createPacket((AvlData[]) data);

		// Split a "physical" packet into several parts by machines,
		// which were active during those messages emitting.
		// After that redirect those packets to appropriate machine actors.
		final Map<MACHINE, Packet<MESSAGE>> machinePackets = splitByMachine(packet);
		for (final Entry<MACHINE, Packet<MESSAGE>> machinePacket : machinePackets.entrySet()) {
		    if (machinePacket.getKey() == null) {
			logger.warn("No machine exists for [" + module.getKey() + "] messages [" + machinePacket.getValue().getMessages() + "]. These messages will be disregarded at this stage.");
		    } else {
			logger.debug("[" + machinePacket.getKey() + "] machine is active for [" + module.getKey() + "] messages [" + machinePacket.getValue().getMessages() + "]. The messages will be redirected to appropriate machine actor.");
			actors.getMachineActor(machinePacket.getKey().getId()).tell(machinePacket.getValue(), getSelf());
		    }
		}
	    } else if (data instanceof New) {
		promoteNewMachineAssociation((New<ASSOCIATION>) data);
	    } else if (data instanceof Changed) {
		promoteChangedMachineAssociation((Changed<ASSOCIATION>) data);
	    } else {
		unhandled(data);
	    }
	} catch (final Exception e) {
	    logger.error(e.getMessage(), e);
	    throw e;
	}
    }

    private static String toString(final Date date) {
	return date == null ? "+\u221E" : dateFormatter.format(date);
    }

    protected void promoteNewMachineAssociation(final New<ASSOCIATION> newAssoc) {
	final ASSOCIATION assoc = newAssoc.getValue();

	machineAssociations.add(assoc);
	Collections.sort(this.machineAssociations, machineAssociationsComparator);

	logger.info("A new association [" + assoc + " to " + toString(assoc.getTo()) + "] has been appeared and sucessfully promoted to module actor.");
    }

    protected void promoteChangedMachineAssociation(final Changed<ASSOCIATION> changedAssoc) {
	final ASSOCIATION assoc = changedAssoc.getValue();

	final ASSOCIATION oldAssoc = findContaining(assoc.getFrom(), machineAssociations, machineAssociationsComparator);
	if (oldAssoc == null) {
	    throw new IllegalStateException("There should be an association instance in the cache that should be updated [" + assoc + " to " + toString(assoc.getTo()) + "]! But there is no such instance.");
	}

	machineAssociations.remove(oldAssoc);
	machineAssociations.add(assoc);
	Collections.sort(this.machineAssociations, machineAssociationsComparator);

	logger.info("An existent association, that has been closed [" + assoc + " to " + toString(assoc.getTo()) + "], has been sucessfully promoted to module actor.");
    }

    /**
     * Splits a packet that has been arrived from module by machines which are active for concrete packet messages.
     *
     * IMPORTANT: <code>null</code> key in this map represents messages which no active machine.
     *
     * @param packet
     * @return
     */
    protected Map<MACHINE, Packet<MESSAGE>> splitByMachine(final Packet<MESSAGE> packet) {
	final Map<MACHINE, Packet<MESSAGE>> machinePackets = new HashMap<MACHINE, Packet<MESSAGE>>();
	final Date packetReceived = new Date(packet.getCreated());
	for (final MESSAGE message : packet.getMessages()) {
	    final Option<MACHINE> activeMachineOption = getActiveMachine(message.getGpsTime());
	    final MACHINE activeMachine = activeMachineOption.value(); // can be null! Null represents a missing machine.
	    if (!machinePackets.containsKey(activeMachine)) {
		machinePackets.put(activeMachine, new Packet<MESSAGE>(packetReceived, messagesComparator));
	    }
	    final MESSAGE messageWithMachine = setMachineForMessage(message, activeMachine);
	    machinePackets.get(activeMachine).add(messageWithMachine);
	}
	return machinePackets;
    }

    /**
     * Finds an association which fully contains the specified 'date'. Returns 'null' if there is no such association.
     *
     * @param date
     * @param establishedIntervals
     * @return
     */
    protected ASSOCIATION findContaining(final Date date, final List<ASSOCIATION> machineAssociations, final Comparator<ASSOCIATION> machineAssociationsComparator) {
	if (date == null) {
	    throw new IllegalArgumentException("Date cannot be 'null'.");
	}
	final int index = Collections.binarySearch(machineAssociations, createSampleModuleAssociation(date), machineAssociationsComparator);

	final int foundAssociationIndex;
	if (index >= 0) {
	    foundAssociationIndex = index;
	} else {
	    final int i = (-index - 1 - 1);
	    foundAssociationIndex = (i >= 0 && i <= machineAssociations.size() - 1) ? i : -1;
	}

	if (foundAssociationIndex < 0) {
	    return null;
	} else {
	    final ASSOCIATION association = machineAssociations.get(foundAssociationIndex);
	    return association.getFrom().getTime() <= date.getTime() &&
		    (association.getTo() == null || date.getTime() < association.getTo().getTime()) ?
		    association : null;
	}
    }

    protected abstract ASSOCIATION createSampleModuleAssociation(final Date date);

    /**
     * Returns an appropriate machine that was active on module during message birth (gpsTime).
     *
     * @param gpsTime
     * @return
     */
    private Option<MACHINE> getActiveMachine(final Date gpsTime) {
	final ASSOCIATION machineAssociation = findContaining(gpsTime, machineAssociations, machineAssociationsComparator);
	return machineAssociation == null ? new Option<MACHINE>(null) : new Option<MACHINE>(machineAssociation.getMachine());
    }

    private Packet<MESSAGE> createPacket(final AvlData[] data) {
	final Date packetReceived = new Date();
	final Packet<MESSAGE> packet = new Packet<MESSAGE>(packetReceived, messagesComparator);
	for (int i = data.length - 1; i >= 0; i--) {
	    final MESSAGE uncompletedMessage = avlToMessageConverter.populateData(createMessage(), data[i], packetReceived);
	    packet.add(uncompletedMessage);
	}
	return packet;
    }

    /**
     * A method to create empty GPS message.
     *
     * @return
     */
    protected abstract MESSAGE createMessage();

    /**
     * Sets an appropriate machine that was active on module during message birth.
     *
     * @param message
     * @param activeMachine
     * @return
     */
    protected abstract MESSAGE setMachineForMessage(final MESSAGE message, final MACHINE activeMachine);

    protected HibernateUtil getHibUtil() {
	return hibUtil;
    }

    public MessagesComparator<MESSAGE> getMessagesComparator() {
	return messagesComparator;
    }
}