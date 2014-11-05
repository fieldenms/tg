package ua.com.fielden.platform.gis.gps.actors;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.persistence.HibernateUtil;
import akka.actor.UntypedActor;

/**
 * This actors corrects the violating messages for all machines. The heavy-weight logic of correcting violating messages has been separated from machine actor to prevent
 * simultaneous hard-disk-intensive database operations and corrections.
 *
 */
public abstract class AbstractViolatingMessageResolverActor<MESSAGE extends AbstractAvlMessage> extends UntypedActor {
    private final Logger logger = Logger.getLogger(AbstractViolatingMessageResolverActor.class);

    private final HibernateUtil hibUtil;

    public AbstractViolatingMessageResolverActor(final HibernateUtil hibUtil) {
        this.hibUtil = hibUtil;
    }

    /**
     * Processes a packet with violating messages.
     *
     * @param packet
     * @throws Exception
     */
    protected abstract void processViolators(final Packet<MESSAGE> packet) throws Exception;

    @Override
    public void onReceive(final Object data) throws Exception {
        try {
            if (data instanceof Packet) {
                final Packet<MESSAGE> packetWithViolatingMessages = (Packet<MESSAGE>) data;
                processViolators(packetWithViolatingMessages);
            } else {
                unhandled(data);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    protected HibernateUtil getHibUtil() {
        return hibUtil;
    }
}
