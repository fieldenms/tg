package ua.com.fielden.platform.sample.domain.composite;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * Testing entity for autocompleter {@link IsProperty#displayAs()} formatting.
 * Major Component will represent highest level of components that a rolling stock consists of.
 *
 * <pre>
 *                                                           TgRollingStockMinorComponent<br>
 *        majorComponent: TgRollingStockMajorComponent                      " "                    minorComponent: TgMinorComponent<br>
 * rollingStockType: String  "/"  majorComponent: String                                       minorComponent: String    ":"   type: String
 * </pre>
 *
 * @author TG Team
 *
 */
@KeyType(value = DynamicEntityKey.class, keyMemberSeparator = " / ")
@KeyTitle("Major Component")
@CompanionObject(TgRollingStockMajorComponentCo.class)
@MapEntityTo
@DescTitle("Description")
public class TgRollingStockMajorComponent extends AbstractPersistentEntity<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgRollingStockMajorComponent.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @Title("Rolling Stock Type")
    private String rollingStockType;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    @Title("Major Component")
    @Optional
    private String majorComponent;

    @Observable
    public TgRollingStockMajorComponent setMajorComponent(final String value) {
        majorComponent = value;
        return this;
    }

    public String getMajorComponent() {
        return majorComponent;
    }

    @Observable
    public TgRollingStockMajorComponent setRollingStockType(final String value) {
        rollingStockType = value;
        return this;
    }

    public String getRollingStockType() {
        return rollingStockType;
    }

}
