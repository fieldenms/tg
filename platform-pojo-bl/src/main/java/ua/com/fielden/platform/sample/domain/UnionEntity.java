package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;

import javax.annotation.Nullable;

@DenyIntrospection
@CompanionObject(IUnionEntity.class)
public class UnionEntity extends AbstractUnionEntity {

    // Pattern matching with sealed interfaces

    public sealed interface U permits EntityOne, EntityTwo {}

    public U value() {
        return (U) activeEntity();
    }

    // Pattern matching without sealed interfaces, with records

    public static @Nullable M1<?> match1(final UnionEntity union) {
        // The body of this method could be generated at runtime.
        return union == null ? null : switch (union.activeEntity()) {
            case EntityOne it -> new M1.EntityOne(it);
            case EntityTwo it -> new M1.EntityTwo(it);
            default -> null;
        };
    }

    public sealed interface M1<T> {
        T get();

        record EntityOne(ua.com.fielden.platform.sample.domain.EntityOne get) implements M1<ua.com.fielden.platform.sample.domain.EntityOne> {}
        record EntityTwo(ua.com.fielden.platform.sample.domain.EntityTwo get) implements M1<ua.com.fielden.platform.sample.domain.EntityTwo> {}
    }

    // Pattern matching without sealed interfaces, with interfaces

    public static @Nullable M2<?> match2(final UnionEntity union) {
        // The body of this method could be generated at runtime.
        return union == null ? null : switch (union.activeEntity()) {
            case EntityOne it -> (M2.EntityOne) () -> it;
            case EntityTwo it -> (M2.EntityTwo) () -> it;
            default -> null;
        };
    }

    public sealed interface M2<T> {
        T get();

        non-sealed interface EntityOne extends M2<ua.com.fielden.platform.sample.domain.EntityOne> {}
        non-sealed interface EntityTwo extends M2<ua.com.fielden.platform.sample.domain.EntityTwo> {}
    }

    @Title(value = "Prop One", desc = "Desc")
    @IsProperty
    @MapTo
    private EntityOne propertyOne;

    @Title(value = "Prop Two", desc = "Desc")
    @IsProperty
    @MapTo
    private EntityTwo propertyTwo;

    public EntityOne getPropertyOne() {
        return propertyOne;
    }

    @Observable
    public UnionEntity setPropertyOne(final EntityOne propertyOne) {
        this.propertyOne = propertyOne;
        return this;
    }

    public EntityTwo getPropertyTwo() {
        return propertyTwo;
    }

    @Observable
    public UnionEntity setPropertyTwo(final EntityTwo propertyTwo) {
        this.propertyTwo = propertyTwo;
        return this;
    }
}
