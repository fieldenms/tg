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
import ua.com.fielden.platform.entity.annotation.titles.PathTitle;
import ua.com.fielden.platform.entity.annotation.titles.Subtitles;

/**
 * Test entity demonstrating the use of {@link Subtitles}.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Leveled Entity No")
@DescTitle(value = "Description")
public class FirstLevelEntityPathDependentTitles extends AbstractEntity<DynamicEntityKey> implements ISomeInterface {

    @IsProperty
    @CompositeKeyMember(2)
    @CritOnly
    @Title("Two")
    private String propertyTwo;

    @IsProperty
    @Title("Property")
    @CompositeKeyMember(1)
    private String property;

    @IsProperty
    @Title("Property")
    @Subtitles(@PathTitle(path = "property", title = "Nested title", desc = "Nested desc"))
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
