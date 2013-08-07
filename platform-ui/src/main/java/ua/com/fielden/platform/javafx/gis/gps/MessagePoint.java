package ua.com.fielden.platform.javafx.gis.gps;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.gis.Point;

/**
 * Represents a {@link Message}'s location.
 *
 * @author TG Team
 *
 */
public class MessagePoint extends Point {
    // FIXME in all concrete implementations MACHINE_PROP_ALIAS should be defined! Use reflection at this stage
    public static /*final */String MACHINE_PROP_ALIAS = null;
    private final AbstractEntity<?> message;
    private final AbstractEntity<?> machine;

    private final int vectorAngle;

    /**
     * Creates a {@link MessagePoint} from a {@link Message} entity.
     *
     * @param message
     * @return
     */
    public static MessagePoint createMessagePointFromMessage(final AbstractEntity<?> message) {
	validateMessage(message);
	validateProperty(message, MACHINE_PROP_ALIAS);
	return new MessagePoint(message);
    }

    /**
     * Creates a {@link MessagePoint} from a {@link Machine} entity.
     *
     * @param machine
     * @return
     */
    public static MessagePoint createMessagePointFromMachine(final AbstractEntity<?> machine) {
	validateProperty(machine, "lastMessage");
	final AbstractEntity<?> message = (AbstractEntity<?>) machine.get("lastMessage");
	validateMessage(message);
	return new MessagePoint(message, machine);
    }

    /**
     * Creates a {@link MessagePoint} from a {@link Machine} entity.
     *
     * @param machine
     * @return
     */
    public static MessagePoint createMessagePointFromMachine(final AbstractEntity<?> machine, final AbstractEntity<?> message) {
	validateMessageWithoutMachine(message);
	return new MessagePoint(message, machine);
    }

    protected static void validateProperty(final AbstractEntity<?> entity, final String property) {
	if (entity.get(property) == null) {
	    throw new IllegalArgumentException("Entity " + entity.getClass().getSimpleName() + " '" + property + "' property should be fetched.");
	}
    }

    protected static void validateMessage(final AbstractEntity<?> message) {
	validateMessageWithoutMachine(message);
	validateProperty(message, MACHINE_PROP_ALIAS);
    }

    protected static void validateMessageWithoutMachine(final AbstractEntity<?> message) {
	validateProperty(message, "created");
	validateProperty(message, "gpsTime");
	validateProperty(message, "vectorSpeed");
	validateProperty(message, "vectorAngle");
	validateProperty(message, "y");
	validateProperty(message, "x");
    }

    private MessagePoint(final AbstractEntity<?> message, final AbstractEntity<?> machine) {
	super((Date) message.get("created"), (Date) message.get("gpsTime"), (Integer) message.get("vectorSpeed"), ((BigDecimal) message.get("y")).doubleValue(), ((BigDecimal) message.get("x")).doubleValue());

	this.vectorAngle = (Integer) message.get("vectorAngle");

	this.message = message;
	this.machine = machine;
    }

    private MessagePoint(final AbstractEntity<?> message) {
	this(message, (AbstractEntity<?>) message.get(MACHINE_PROP_ALIAS));
    }

    @Override
    public String toString() {
	return "MessagePoint [machine = " + message.get(MACHINE_PROP_ALIAS) + "]" + super.toString();
    }

    public AbstractEntity<?> getMessage() {
	return message;
    }

    public AbstractEntity getMachine() {
	return machine;
    }

    public int getVectorAngle() {
	return vectorAngle;
    }

    @Override
    public int compareTo(final Point o) {
	final int machineCompareTo = getMachine().compareTo(((MessagePoint) o).getMachine());
        return machineCompareTo == 0 ? super.compareTo(o) : machineCompareTo;
    }
}
