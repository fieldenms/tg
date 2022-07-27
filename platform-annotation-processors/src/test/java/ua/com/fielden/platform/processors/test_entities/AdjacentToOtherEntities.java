package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
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
public class AdjacentToOtherEntities extends AbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(AdjacentToOtherEntities.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    @IsProperty
    @MapTo
    @Title(value = "Prop1")
    private String prop1;
    
    @IsProperty
    @MapTo
    @Title(value = "Entity with desc title")
    private WithDescTitle entity1;
    
    @IsProperty
    @MapTo
    @Title(value = "Entity with sink nodes only")
    private SinkNodesOnly entity2;

    @Observable
    public AdjacentToOtherEntities setEntity2(final SinkNodesOnly entity2) {
        this.entity2 = entity2;
        return this;
    }

    public SinkNodesOnly getEntity2() {
        return entity2;
    }

    @Observable
    public AdjacentToOtherEntities setEntity1(final WithDescTitle entity1) {
        this.entity1 = entity1;
        return this;
    }

    public WithDescTitle getEntity1() {
        return entity1;
    }

    @Observable
    public AdjacentToOtherEntities setProp1(final String prop1) {
        this.prop1 = prop1;
        return this;
    }

    public String getName() {
        return prop1;
    }
}
