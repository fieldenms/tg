package ua.com.fielden.platform.processors.test_entities;

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
 * A test entity representing a sub-type of {@link KeyType_AbstractSuperEntityWithoutKeyType} with a key of an entity type (the actual type is not important).
 *
 * @author TG Team
 *
 */
@KeyType(SubEntity.class)
@KeyTitle("Key")
@MapEntityTo
@DescTitle("Description")
public class KeyTypeAsEntity_SubEntityExtendingAbstractSuperEntityWithoutKeyType extends KeyType_AbstractSuperEntityWithoutKeyType<SubEntity> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(KeyTypeAsEntity_SubEntityExtendingAbstractSuperEntityWithoutKeyType.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    @IsProperty
    @MapTo
    @Title(value = "Prop 2", desc = "Some prop")
    private String prop2;

    @Observable
    public KeyTypeAsEntity_SubEntityExtendingAbstractSuperEntityWithoutKeyType setProp2(final String prop2) {
        this.prop2 = prop2;
        return this;
    }

    public String getProp2() {
        return prop2;
    }

}