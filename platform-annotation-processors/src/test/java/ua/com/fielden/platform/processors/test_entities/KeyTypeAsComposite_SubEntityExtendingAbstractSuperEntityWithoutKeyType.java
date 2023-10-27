package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test entity representing a sub-type of {@link KeyType_AbstractSuperEntityWithoutKeyType} with a composite key.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Key")
@MapEntityTo
@DescTitle("Description")
public class KeyTypeAsComposite_SubEntityExtendingAbstractSuperEntityWithoutKeyType extends KeyType_AbstractSuperEntityWithoutKeyType<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(KeyTypeAsComposite_SubEntityExtendingAbstractSuperEntityWithoutKeyType.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @Title(value = "Prop 2", desc = "Some prop")
    private String prop2;

    @Observable
    public KeyTypeAsComposite_SubEntityExtendingAbstractSuperEntityWithoutKeyType setProp2(final String prop2) {
        this.prop2 = prop2;
        return this;
    }

    public String getProp2() {
        return prop2;
    }

}
