package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Test class, which represent an entity derived directly from AbstractEntity.
 *
 * @author 01es
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(FirstLevelEntity.KEY_TITLE)
@DescTitle(FirstLevelEntity.DESC_TITLE)
public class FirstLevelEntity extends AbstractEntity<DynamicEntityKey> implements ISomeInterface {

    public static final String KEY_TITLE = "Leveled Entity No";
    public static final String DESC_TITLE = "Description";

    @IsProperty
    @CompositeKeyMember(2)
    @CritOnly
    @MapTo
    @Title("Two")
    private String propertyTwo;

    @IsProperty
    @Title("Property")
    @CompositeKeyMember(1)
    private String property;

    @IsProperty
    @CritOnly
    private SimpleEntity critOnlyAEProperty;

    public String getProperty() {
        return property;
    }

    @Observable
    public void setProperty(final String property) {
        this.property = property;
    }

    public String getPropertyTwo() {
        return propertyTwo;
    }

    @Observable
    public void setPropertyTwo(final String propertyTwo) {
        this.propertyTwo = propertyTwo;
    }

    public boolean methodFirstLevel() {
        return true;
    }

    public SimpleEntity getCritOnlyAEProperty() {
        return critOnlyAEProperty;
    }

    @Observable
    public void setCritOnlyAEProperty(final SimpleEntity critOnlyAEProperty) {
        this.critOnlyAEProperty = critOnlyAEProperty;
    }
}
