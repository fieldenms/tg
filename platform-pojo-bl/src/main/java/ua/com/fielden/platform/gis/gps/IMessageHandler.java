package ua.com.fielden.platform.gis.gps;


/**
 * A contract for processing GPS related data for a module.
 *
 * @author TG Team
 *
 */
public interface IMessageHandler<MODULE extends AbstractAvlModule> {
    IMessageHandler<MODULE> handle(final MODULE module, final AvlData[] data);
}
