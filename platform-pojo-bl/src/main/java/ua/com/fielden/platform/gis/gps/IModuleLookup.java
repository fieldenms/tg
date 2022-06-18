package ua.com.fielden.platform.gis.gps;

import java.util.Optional;

/**
 * A contract for lookin up a module by its IMEI.
 * 
 * @author TG Team
 * 
 */
public interface IModuleLookup<MODULE extends AbstractAvlModule> {
    Optional<MODULE> get(final String imei);
}