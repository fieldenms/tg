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
public class FirstReferencedClass extends AbstractEntity<String> {

    private static final long serialVersionUID = 3563995343391287908L;

    @IsProperty
    @Title(value = "value", desc = "description")
    private Reference<String> referenceProperty;

    @IsProperty
    @Title(value = "value", desc = "description")
    private Reference<String> firstProperty;

    public Reference<String> getReferenceProperty() {
        return referenceProperty;
    }

    @Observable
    public void setReferenceProperty(final Reference<String> referenceProperty) {
        this.referenceProperty = referenceProperty;
    }

    public Reference<String> getFirstProperty() {
        return firstProperty;
    }

    @Observable
    public void setFirstProperty(final Reference<String> firstProperty) {
        this.firstProperty = firstProperty;
    }

}
