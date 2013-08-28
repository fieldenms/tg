package ua.com.fielden.platform.gis.gps.factory;

import java.util.Map;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.IMachineLookup;
import ua.com.fielden.platform.gis.gps.Option;

/**
 * An implementation of contract {@link IMachineLookup} that looks up a machine by IMEI in the provided map.
 *
 * @author TG Team
 *
 */
public class MachineLookupDao<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>> implements IMachineLookup<T, M> {
    private final Map<String, M> cache;

    public MachineLookupDao(final Map<String, M> cache) {
	this.cache = cache;
    }

    @Override
    public Option<M> get(final String imei) {
	return new Option<M>(cache.get(imei));
    }
}