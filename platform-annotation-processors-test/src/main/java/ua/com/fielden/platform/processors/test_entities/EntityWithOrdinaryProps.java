package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

import java.util.Date;

/**
 * A test entity, which only has properties of ordinary types, modelled with {@link PropertyMetaModel}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
@DescTitle("Description")
@DisplayDescription
@DescRequired
public class EntityWithOrdinaryProps extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(EntityWithOrdinaryProps.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private String prop1;

    @IsProperty
    @MapTo
    @Title(value = "Prop2")
    private Date prop2;

    @Observable
    public EntityWithOrdinaryProps setProp1(final String prop1) {
        this.prop1 = prop1;
        return this;
    }

    public String getProp1() {
        return prop1;
    }

    @Observable
    public EntityWithOrdinaryProps setProp2(final Date prop2) {
        this.prop2 = prop2;
        return this;
    }

    public Date getProp2() {
        return prop2;
    }

}
