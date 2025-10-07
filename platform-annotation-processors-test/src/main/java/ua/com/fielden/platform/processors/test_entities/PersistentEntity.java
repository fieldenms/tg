package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity representing a persistent entity.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
@DescTitle("Description")
public class PersistentEntity extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(PersistentEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private Integer prop1;

    @IsProperty
    @MapTo
    @Title("A property that exists to manifest a common union property")
    private String common1;

    @IsProperty
    @MapTo
    @Title("A property that exists to manifest a common union property")
    private EntityWithOrdinaryProps common2;

    public EntityWithOrdinaryProps getCommon2() {
        return common2;
    }

    @Observable
    public PersistentEntity setCommon2(final EntityWithOrdinaryProps common2) {
        this.common2 = common2;
        return this;
    }

    public String getCommon1() {
        return common1;
    }

    @Observable
    public PersistentEntity setCommon1(final String common1) {
        this.common1 = common1;
        return this;
    }

    @Observable
    public PersistentEntity setProp1(final Integer prop1) {
        this.prop1 = prop1;
        return this;
    }

    public Integer getProp1() {
        return prop1;
    }

}
