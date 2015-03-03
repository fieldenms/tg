package ua.com.fielden.platform.serialisation.jackson;

import java.util.LinkedHashMap;

/**
 * Utility to register entity types with sequential numbers (type table).
 *
 * @author TG Team
 *
 */
public class EntityTypeInfoGetter {
    private final LinkedHashMap<Long, EntityType> typeTable = new LinkedHashMap<>();
    private final LinkedHashMap<String, EntityType> typeTableByName = new LinkedHashMap<>();
    private Long typeCount = 1L;

    public EntityType get(final Long typeNumber) {
        return typeTable.get(typeNumber);
    }

    public EntityType get(final String typeName) {
        return typeTableByName.get(typeName);
    }

    /**
     * Registers the <code>entityType</code> and gives it the number.
     *
     * @param entityType
     */
    public EntityType register(final EntityType entityType) {
        if (!typeTableByName.containsKey(entityType.getKey())) {
            typeCount = typeCount + 1;
            typeTable.put(typeCount, entityType.set_number(typeCount)); // starting from 2 (0 and 1 are reserved for special types EntityType and EntityTypeProp)
            typeTableByName.put(entityType.getKey(), entityType);
            entityType.endInitialising();

            System.err.println("Registering new type with number [" + entityType.get_number() + "] = " + entityType);
        } else {
            System.err.println("Is trying to register already registered new type [" + entityType.getKey() + "]. Has disregarded.");
        }
        return typeTableByName.get(entityType.getKey());
    }

    /**
     * Returns a map of numbers and corresponded entity types.
     *
     * @return
     */
    public LinkedHashMap<Long, EntityType> getTypeTable() {
        return typeTable;
    }
}
