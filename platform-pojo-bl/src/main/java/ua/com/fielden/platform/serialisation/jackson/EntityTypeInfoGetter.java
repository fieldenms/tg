package ua.com.fielden.platform.serialisation.jackson;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

/**
 * Utility to register entity types by their full class names (type table).
 *
 * @author TG Team
 *
 */
public class EntityTypeInfoGetter {
    private final LinkedHashMap<String, EntityType> typeTable = new LinkedHashMap<>();
    private final Logger logger = Logger.getLogger(getClass());

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

            logger.debug(String.format("Registering new type with name [%s] = %s", entityType.getKey(), entityType));
        } else {
            logger.warn(String.format("Is trying to register already registered new type [%s]. Has disregarded.", entityType.getKey()));
        }
        return typeTable.get(entityType.getKey());
    }

    /**
     * Returns a map of full class names and corresponding entity types.
     *
     * @return
     */
    public LinkedHashMap<String, EntityType> getTypeTable() {
        return typeTable;
    }
}
