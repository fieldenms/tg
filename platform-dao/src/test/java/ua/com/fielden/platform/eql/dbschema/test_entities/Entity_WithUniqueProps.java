package ua.com.fielden.platform.eql.dbschema.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// A persistent test entity with `@Unique` properties, both nullable and not.
/// Used to verify generation of unique indices, which is RDBMS-specific for nullable columns.
///
@MapEntityTo
@KeyType(String.class)
public class Entity_WithUniqueProps extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Unique
    private String nullableUnique;

    @IsProperty
    @MapTo
    @Unique
    private boolean booleanUnique;

    /// A column of an unbounded length, which SQL Server cannot index.
    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    @Unique
    private String unboundedUnique;

    public String getUnboundedUnique() {
        return unboundedUnique;
    }

    @Observable
    public Entity_WithUniqueProps setUnboundedUnique(final String unboundedUnique) {
        this.unboundedUnique = unboundedUnique;
        return this;
    }

    public String getNullableUnique() {
        return nullableUnique;
    }

    @Observable
    public Entity_WithUniqueProps setNullableUnique(final String nullableUnique) {
        this.nullableUnique = nullableUnique;
        return this;
    }

    public boolean getBooleanUnique() {
        return booleanUnique;
    }

    @Observable
    public Entity_WithUniqueProps setBooleanUnique(final boolean booleanUnique) {
        this.booleanUnique = booleanUnique;
        return this;
    }

}
