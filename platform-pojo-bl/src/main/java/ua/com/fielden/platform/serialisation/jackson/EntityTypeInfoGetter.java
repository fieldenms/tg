package ua.com.fielden.platform.serialisation.jackson;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Utility to register entity types by their full class names (type table).
 *
 * @author TG Team
 *
 */
public final class EntityTypeInfoGetter {
    private final Map<String, EntityType> typeTable = new LinkedHashMap<>();
    private static final Logger logger = Logger.getLogger(EntityTypeInfoGetter.class);

    public EntityType get(final String typeName) {
        return typeTable.get(typeName);
    }

    /**
     * Registers the <code>entityType</code> by its full class name.
     *
     * @param entityType
     */
    public EntityType register(final EntityType entityType) {
        if (!typeTable.containsKey(entityType.getKey())) {
            typeTable.put(entityType.getKey(), entityType);
            entityType.endInitialising();

            logger.debug(format("Registering new type with name [%s] = %s", entityType.getKey(), entityType));
        } else {
            logger.warn(format("Is trying to register already registered new type [%s]. Has disregarded.", entityType.getKey()));
        }
        return typeTable.get(entityType.getKey());
    }

    /**
     * Returns a map of full class names and corresponding entity types.
     *
     * @return
     */
    public Map<String, EntityType> getTypeTable() {
        return unmodifiableMap(typeTable);
    }
}
