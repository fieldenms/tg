package ua.com.fielden.platform.gis.gps;


/**
 * A contract for lookin up a module by its IMEI.
 *
 * @author TG Team
 *
 */
public interface IModuleLookup<MODULE extends AbstractAvlModule> {
    Option<MODULE> get(final String imei);
}
