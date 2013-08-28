package ua.com.fielden.platform.gis.gps;


/**
 * A contract for persisting GPS related data for a machine.
 *
 * @author TG Team
 *
 */
public interface IMessageHandler<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>> {
    IMessageHandler<T, M> handle(final M machine, final AvlData[] data);
}
