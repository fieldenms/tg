package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity representing a sub-type extending {@link SuperEntity}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
@DescTitle("Description")
public class SubEntity extends SuperEntity {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(SubEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private int prop1;

    @IsProperty
    @MapTo
    @Title(value = "Parent entity", desc = "Extended_description")
    private SuperEntity parent;

    @Observable
    public SubEntity setParent(final SuperEntity parent) {
        this.parent = parent;
        return this;
    }

    public SuperEntity getParent() {
        return parent;
    }

    @Observable
    public SubEntity setProp1(final int prop1) {
        this.prop1 = prop1;
        return this;
    }

    public int getProp1() {
        return prop1;
    }

}