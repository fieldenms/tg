package ua.com.fielden.platform.gis.gps;

/**
 * A contract for processing GPS related data for a module.
 * 
 * @author TG Team
 * 
 */
public interface IMessageHandler {
    IMessageHandler handle(final String imei, final AvlData[] data);
}
