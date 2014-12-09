package ua.com.fielden.platform.serialisation.jackson;

import java.util.LinkedHashMap;

public class EntityTypeInfoGetter {
    private final LinkedHashMap<Long, EntityType> typeTable = new LinkedHashMap<>();
    private Long typeCount = 1L;

    public EntityType get(final Long typeNumber) {
        return typeTable.get(typeNumber);
    }

    public void register(final EntityType entityTypeInfo) {
        typeCount = typeCount + 1;
        typeTable.put(typeCount, entityTypeInfo.set_number(typeCount)); // starting from 2
        entityTypeInfo.endInitialising();
    }

    public LinkedHashMap<Long, EntityType> getTypeTable() {
        return typeTable;
    }
}
