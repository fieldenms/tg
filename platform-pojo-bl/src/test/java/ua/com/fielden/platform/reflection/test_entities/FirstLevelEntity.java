package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Test class, which represent an entity derived directly from AbstractEntity.
 * 
 * @author 01es
 * 
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Leveled Entity No")
@DescTitle(value = "Description")
public class FirstLevelEntity extends AbstractEntity<DynamicEntityKey> implements ISomeInterface {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title("Property")
    @CompositeKeyMember(1)
    private String property;

    @IsProperty
    @CompositeKeyMember(2)
    @CritOnly
    private String propertyTwo;

    @IsProperty
    @CritOnly
    private SimpleEntity critOnlyAEProperty;

    protected FirstLevelEntity(final Long id, final DynamicEntityKey key, final String desc) {
        super(null, null, "");
    }

    protected FirstLevelEntity() {
        super(null, null, "");
        setKey(new DynamicEntityKey(this));
    }

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
