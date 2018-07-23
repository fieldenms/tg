package ua.com.fielden.platform.serialisation.jackson;

import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;

/**
 * Utility to register entity types by their full class names (type table).
 *
 * @author TG Team
 *
 */
public final class EntityTypeInfoGetter {
    private static final Logger LOGGER = Logger.getLogger(EntityTypeInfoGetter.class);

    private final ConcurrentHashMap<String, EntityType> typeTable = new ConcurrentHashMap<>(512);

    public EntityType get(final String typeName) {
        return typeTable.get(typeName);
    }

    /**
     * Registers the <code>entityType</code> by its full class name.
     *
     * @param entityType
     */
    public EntityType register(final EntityType entityType) {
        // lets avoid registering EntityType for generated types as not needed
        if (!entityType.getKey().contains(DynamicTypeNamingService.APPENDIX)) {
            typeTable.putIfAbsent(entityType.getKey(), entityType);
            LOGGER.debug(format("Registering new type with name [%s] = %s", entityType.getKey(), entityType));
        }

        return entityType;
    }

    /**
     * Returns a map of full class names and corresponding entity types, which is primarily used for serialisation and sending to the client.
     *
     * @return
     */
    public Map<String, EntityType> getTypeTable() {
        // before, when typeTable was containing instances representing generated types, it was required to purge it before returning for serialisation
        //final Map<String, EntityType> cleaned = typeTable.entrySet().stream().filter(v -> !v.getKey().contains(DynamicTypeNamingService.APPENDIX))
        //        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        
        return typeTable;
    }
}
