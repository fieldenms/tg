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
    private final SortedSet<T> messages;
    private final Set<Long> gpsTimes = new HashSet<>();

    public Blackout(final MessagesComparator<T> messagesComparator) {
        this.messages = new TreeSet<>(messagesComparator);
    }

    public void add(final Packet<T> packet) {
        for (final T message : packet.getMessages()) {
            if (!gpsTimes.contains(message.getGpsTime().getTime())) {
                messages.add(message);
                gpsTimes.add(message.getGpsTime().getTime());
            } else {
                final boolean rm = messages.remove(message);
                final boolean ad = messages.add(message);
            }
        }
    }

    public Collection<T> reset() {
        if (messages.size() > 0) {
            final List<T> result = new ArrayList<>();
            for (final T message : messages) {
                result.add(message);
            }
            messages.clear();
            gpsTimes.clear();
            return result;
        } else {
            return Collections.emptySet();
        }
    }

    public SortedSet<T> getMessages() {
        return messages;
    }

    public T getStart() {
        return messages.isEmpty() ? null : messages.first();
    }

    public T getFinish() {
        return messages.isEmpty() ? null : messages.last();
    }
}