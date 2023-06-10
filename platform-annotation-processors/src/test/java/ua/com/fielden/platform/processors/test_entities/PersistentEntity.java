package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
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

    @Observable
    public PersistentEntity setProp1(final Integer prop1) {
        this.prop1 = prop1;
        return this;
    }

    public Integer getProp1() {
        return prop1;
    }

}