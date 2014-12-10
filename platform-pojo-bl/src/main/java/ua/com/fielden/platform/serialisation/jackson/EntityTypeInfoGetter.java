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
    private Long typeCount = 1L;

    public EntityType get(final Long typeNumber) {
        return typeTable.get(typeNumber);
    }

    /**
     * Registers the <code>entityType</code> and gives it the number.
     *
     * @param entityType
     */
    public void register(final EntityType entityType) {
        typeCount = typeCount + 1;
        typeTable.put(typeCount, entityType.set_number(typeCount)); // starting from 2 (0 and 1 are reserved for special types EntityType and EntityTypeProp)
        entityType.endInitialising();
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
