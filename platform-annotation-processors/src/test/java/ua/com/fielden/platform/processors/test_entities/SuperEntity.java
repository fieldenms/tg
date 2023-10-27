package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

import java.util.Date;

/**
 * A test entity, which represents a super type for entities extending it.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
@DescTitle("Description")
public class SuperEntity extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(SuperEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private String prop1;

    @IsProperty
    @MapTo
    @Title(value = "Prop2", desc = "Extended_description")
    private Date prop2;

    @Observable
    public SuperEntity setProp2(final Date prop2) {
        this.prop2 = prop2;
        return this;
    }

    public Date getProp2() {
        return prop2;
    }

    @Observable
    public SuperEntity setProp1(final String prop1) {
        this.prop1 = prop1;
        return this;
    }

    public String getProp1() {
        return prop1;
    }

}
