package ua.com.fielden.platform.gis.gps.actors;

import java.util.Comparator;

import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;

/**
 * A comparator for GPS messages.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class MessagesComparator<T extends AbstractAvlMessage> implements Comparator<T> {
    @Override
    public int compare(final T p1, final T p2) {
        if (p1.getGpsTime().getTime() > p2.getGpsTime().getTime()) {
            return 1;
        } else if (p1.getGpsTime().getTime() < p2.getGpsTime().getTime()) {
            return -1;
        } else {
            return 0;
        }
    }
};