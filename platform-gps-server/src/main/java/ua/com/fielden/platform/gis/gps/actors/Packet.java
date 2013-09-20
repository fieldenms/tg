package ua.com.fielden.platform.gis.gps.actors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;

/**
 * A packet of chronologically sorted messages.
 *
 */
public class Packet<T extends AbstractAvlMessage> {
    private static BigDecimal TWO = new BigDecimal(2);
    private static BigDecimal TWO_MINUS = new BigDecimal(-2);

    private final Long created;
    private T start;
    private T finish;
    private final SortedSet<T> messages;
    private final Set<Long> gpsTimes = new HashSet<>();

    @Override
    public String toString() {
	return messages.size() == 1 ? ("packet: " + start) : ("packet: " + start + " - " + finish + " (" + messages.size() + ")");
    }

    public Packet(final Date created, final MessagesComparator<T> messagesComparator) {
	super();
	this.created = created.getTime();
	this.messages = new TreeSet<>(messagesComparator);
    }

    public void add(final T message) {
	if (!gpsTimes.contains(message.getGpsTime().getTime()) && isValid(message)) {
	    gpsTimes.add(message.getGpsTime().getTime());
	    messages.add(message);
	    if (start == null || (start != null && message.getGpsTime().getTime() < start.getGpsTime().getTime())) {
		start = message;
	    }

	    if (finish == null || (finish != null && message.getGpsTime().getTime() > finish.getGpsTime().getTime())) {
		finish = message;
	    }
	}
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

    private boolean isZero(final BigDecimal value) {
	return BigDecimal.ZERO.compareTo(value) == 0;
    }

    private boolean isNearZero(final BigDecimal value) {
	return TWO_MINUS.compareTo(value) < 0 && TWO.compareTo(value) > 0;
    }
    public boolean isEmpty() {
	return messages.size() == 0;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((created == null) ? 0 : created.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof Packet)) {
	    return false;
	}
	final Packet<T> other = (Packet<T>) obj;
	if (created == null) {
	    if (other.created != null) {
		return false;
	    }
	} else if (!created.equals(other.created)) {
	    return false;
	}
	return true;
    }

    public T getStart() {
	return start;
    }

    public T getFinish() {
	return finish;
    }

    public SortedSet<T> getMessages() {
	return messages;
    }

    public Long getCreated() {
	return created;
    }
}
