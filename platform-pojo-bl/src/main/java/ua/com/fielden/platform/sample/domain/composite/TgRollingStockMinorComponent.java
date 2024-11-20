package ua.com.fielden.platform.sample.domain.composite;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * Testing entity for autocompleter {@link IsProperty#displayAs()} formatting.
 * Minor Component will represent a sub-component of a rolling stock component.
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
@KeyType(DynamicEntityKey.class)
@KeyTitle("Minor Component")
@CompanionObject(TgRollingStockMinorComponentCo.class)
@MapEntityTo
@DescTitle("Description")
@DisplayDescription
public class TgRollingStockMinorComponent extends AbstractPersistentEntity<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgRollingStockMinorComponent.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    @Title("Major Component")
    private TgRollingStockMajorComponent majorComponent;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    @Title("Minor Component")
    @Optional
    private TgMinorComponent minorComponent;

    @Observable
    public TgRollingStockMinorComponent setMinorComponent(final TgMinorComponent value) {
        minorComponent = value;
        return this;
    }

    public TgMinorComponent getMinorComponent() {
        return minorComponent;
    }

    @Observable
    public TgRollingStockMinorComponent setMajorComponent(final TgRollingStockMajorComponent value) {
        majorComponent = value;
        return this;
    }

    public TgRollingStockMajorComponent getMajorComponent() {
        return majorComponent;
    }

}
