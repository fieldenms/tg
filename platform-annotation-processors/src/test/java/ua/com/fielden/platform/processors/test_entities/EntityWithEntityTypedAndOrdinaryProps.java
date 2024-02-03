package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity, which consists of a mix of properties -- entity-types ones and ordinary ones.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
@DescTitle("Desc")
public class EntityWithEntityTypedAndOrdinaryProps extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(EntityWithEntityTypedAndOrdinaryProps.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private String prop1;

    @IsProperty
    @MapTo
    @Title(value = "Entity with desc title")
    private EntityWithDescTitle entity1;

    @IsProperty
    @MapTo
    @Title(value = "Entity with sink nodes only")
    private EntityWithOrdinaryProps entity2;

    @Observable
    public EntityWithEntityTypedAndOrdinaryProps setEntity2(final EntityWithOrdinaryProps entity2) {
        this.entity2 = entity2;
        return this;
    }

    public EntityWithOrdinaryProps getEntity2() {
        return entity2;
    }

    @Observable
    public EntityWithEntityTypedAndOrdinaryProps setEntity1(final EntityWithDescTitle entity1) {
        this.entity1 = entity1;
        return this;
    }

    public EntityWithDescTitle getEntity1() {
        return entity1;
    }

    @Observable
    public EntityWithEntityTypedAndOrdinaryProps setProp1(final String prop1) {
        this.prop1 = prop1;
        return this;
    }

    public String getProp1() {
        return prop1;
    }

}
