package ua.com.fielden.platform.reflection.test_entities;

import java.lang.ref.Reference;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "key", desc = "key description")
@DescTitle(value = "desc", desc = "desc description")
public class SecondReferencedClass extends AbstractEntity<String> {

    private static final long serialVersionUID = 6506434952610985044L;

    @IsProperty
    @Title(value = "value", desc = "description")
    private Reference<String> referenceProperty;

    @IsProperty
    @Title(value = "value", desc = "description")
    private Reference<String> secondProperty;

    public Reference<String> getReferenceProperty() {
        return referenceProperty;
    }

    @Observable
    public void setReferenceProperty(final Reference<String> referenceProperty) {
        this.referenceProperty = referenceProperty;
    }

    public Reference<String> getSecondProperty() {
        return secondProperty;
    }

    @Observable
    public void setSecondProperty(final Reference<String> secondProperty) {
        this.secondProperty = secondProperty;
    }

}
