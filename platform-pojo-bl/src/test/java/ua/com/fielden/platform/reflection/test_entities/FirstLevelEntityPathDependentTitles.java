package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
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
    @Title("Property 1")
    @Subtitles(@PathTitle(path = "property", title = "Nested title", desc = "Nested desc"))
    private SimpleEntity prop1;
    
    @IsProperty
    @MapTo
    @Title("Property 2")
    @Subtitles({@PathTitle(path = "critOnlyAEProperty", title = "First Level Nested Title", desc = "First Level Nested Desc"),
                @PathTitle(path = "critOnlyAEProperty.propertyTwo", title = "Second Level Nested Title", desc = "Second Level Nested Desc")})
    private FirstLevelEntity prop2;

    @Observable
    public FirstLevelEntityPathDependentTitles setProp2(final FirstLevelEntity prop2) {
        this.prop2 = prop2;
        return this;
    }

    public SimpleEntity getProp1() {
        return prop1;
    }

    @Observable
    public void setProp1(final SimpleEntity critOnlyAEProperty) {
        this.prop1 = critOnlyAEProperty;
    }
    
    public FirstLevelEntity getProp2() {
        return prop2;
    }

    public boolean methodFirstLevel() {
        return true;
    }

}
