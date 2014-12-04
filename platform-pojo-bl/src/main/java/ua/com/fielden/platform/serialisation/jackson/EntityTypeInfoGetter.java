package ua.com.fielden.platform.serialisation.jackson;

import java.util.LinkedHashMap;

public class EntityTypeInfoGetter {
    private final LinkedHashMap<Long, EntityTypeInfo> typeTable = new LinkedHashMap<>();
    private Long typeCount = 0L;

    public EntityTypeInfo get(final Long typeNumber) {
        return typeTable.get(typeNumber);
    }

    public void register(final EntityTypeInfo entityTypeInfo) {
        typeCount = typeCount + 1;
        typeTable.put(typeCount, entityTypeInfo.setNumber(typeCount)); // starting from 1
    }

    public LinkedHashMap<Long, EntityTypeInfo> getTypeTable() {
        return typeTable;
    }
}
