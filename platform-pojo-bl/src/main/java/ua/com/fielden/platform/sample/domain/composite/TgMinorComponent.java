package ua.com.fielden.platform.sample.domain.composite;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * Testing entity for autocompleter {@link IsProperty#displayAs()} formatting.
 * Minor Component will represent lowest level of components that a rolling stock consists of.
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
@KeyType(value = DynamicEntityKey.class, keyMemberSeparator = ":")
@KeyTitle("Minor Component")
@CompanionObject(TgMinorComponentCo.class)
@MapEntityTo
@DescTitle("Description")
public class TgMinorComponent extends AbstractPersistentEntity<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgMinorComponent.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title("Minor Component")
    @CompositeKeyMember(1)
    private String minorComponent;

    @IsProperty
    @MapTo
    @Title("Type")
    @CompositeKeyMember(2)
    private String minorType;

    @Observable
    public TgMinorComponent setMinorType(final String value) {
        minorType = value;
        return this;
    }

    public String getMinorType() {
        return minorType;
    }

    @Observable
    public TgMinorComponent setMinorComponent(final String value) {
        minorComponent = value;
        return this;
    }

    public String getMinorComponent() {
        return minorComponent;
    }

}
