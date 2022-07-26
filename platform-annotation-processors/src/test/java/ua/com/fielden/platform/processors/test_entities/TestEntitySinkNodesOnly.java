package ua.com.fielden.platform.processors.test_entities;

import java.util.Date;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
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
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@MapEntityTo
@DomainEntity
@DescTitle("Description")
@DisplayDescription
@DescRequired
public class TestEntitySinkNodesOnly extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TestEntitySinkNodesOnly.class);
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
    public TestEntitySinkNodesOnly setProp1(final String prop1) {
        this.prop1 = prop1;
        return this;
    }

    public String getName() {
        return prop1;
    }

    @Observable
    public TestEntitySinkNodesOnly setProp2(final Date prop2) {
        this.prop2 = prop2;
        return this;
    }

    public Date getProp2() {
        return prop2;
    }
}
