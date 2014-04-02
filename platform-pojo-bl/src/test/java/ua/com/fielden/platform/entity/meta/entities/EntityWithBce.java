package ua.com.fielden.platform.entity.meta.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;

/**
 * Entity with BCE handlers, used for meta-property logic testing.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityWithBce extends AbstractEntity<String> {
    @IsProperty
    @Title(value = "Property 1", desc = "Description")
    @BeforeChange(@Handler(value = BceNotPermittedValue.class, str = { @StrParam(name = "notPermittedValue", value = "failure") }))
    private String propWithBce = "default value";

    @IsProperty
    @Title(value = "Property 2", desc = "Description")
    private String property2;

    @Observable
    public EntityWithBce setProperty2(final String property2) {
        this.property2 = property2;
        return this;
    }

    public String getProperty2() {
        return property2;
    }

    @Observable
    public EntityWithBce setPropWithBce(final String property) {
        this.propWithBce = property;
        return this;
    }

    public String getPropWithBce() {
        return propWithBce;
    }
}
