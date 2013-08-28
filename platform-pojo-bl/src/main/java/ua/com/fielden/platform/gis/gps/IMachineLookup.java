package ua.com.fielden.platform.gis.gps;


/**
 * A contract for lookin up a machine by its IMEI.
 *
 * @author TG Team
 *
 */
public interface IMachineLookup<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>> {
    Option<M> get(final String imei);
}
