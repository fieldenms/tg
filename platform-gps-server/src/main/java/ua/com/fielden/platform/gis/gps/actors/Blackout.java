package ua.com.fielden.platform.gis.gps.actors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;

/**
 * A holder of "blackout" messages.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class Blackout<T extends AbstractAvlMessage> {
    private T start;
    private T finish;
    private final List<T> messages = new ArrayList<>();
    private final List<Packet<T>> packets = new ArrayList<>();
    private final MessagesComparator<T> messagesComparator;

    public Blackout(final MessagesComparator<T> messagesComparator) {
	this.messagesComparator = messagesComparator;
    }

    public void add(final Packet<T> packet) {
	packets.add(packet);
	messages.addAll(packet.getMessages());
	if (start == null || (start != null && packet.getStart().getGpsTime().getTime() < start.getGpsTime().getTime())) {
	    start = packet.getStart();
	}

	if (finish == null || (finish != null && packet.getFinish().getGpsTime().getTime() > finish.getGpsTime().getTime())) {
	    finish = packet.getFinish();
	}
    }

    public Collection<T> reset() {
	if (messages.size() > 0) {
	    final Set<Long> gpsTimes = new HashSet<>();
	    final SortedSet<T> result = new TreeSet<T>(messagesComparator);
	    for (final T message : messages) {
		if (!gpsTimes.contains(message.getGpsTime().getTime())) {
		    result.add(message);
		    gpsTimes.add(message.getGpsTime().getTime());
		}

	    }
	    start = null;
	    finish = null;
	    messages.clear();
	    packets.clear();
	    return result;
	} else {
	    return Collections.emptySet();
	}
    }

    public List<T> getMessages() {
	return messages;
    }

    public T getStart() {
	return start;
    }

    public T getFinish() {
	return finish;
    }
}
