package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity that is non-persistent, but a domain entity due to {@code @DomainEntity}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@DomainEntity
@DescTitle("Description")
public class NonPersistentButDomainEntity extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(NonPersistentButDomainEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private Integer prop1;

    @Observable
    public NonPersistentButDomainEntity setProp1(final Integer prop1) {
        this.prop1 = prop1;
        return this;
    }

    public Integer getProp1() {
        return prop1;
    }

}
