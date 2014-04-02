package ua.com.fielden.platform.test.entities;

import ua.com.fielden.platform.entity.SyntheticEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(String.class)
@KeyTitle(value = "key title", desc = "key description")
@DescTitle(value = "desc title", desc = "desc description")
public class ComplexSyntheticEntity extends SyntheticEntity {

    private static final long serialVersionUID = -3094885199753790839L;

    @IsProperty
    private CompositeEntity firstComponent;

    @IsProperty
    private ComplexKeyEntity secondComponent;

    public CompositeEntity getFirstComponent() {
        return firstComponent;
    }

    public ComplexKeyEntity getSecondComponent() {
        return secondComponent;
    }

    @Observable
    public void setFirstComponent(final CompositeEntity firstComponent) {
        this.firstComponent = firstComponent;
    }

    @Observable
    public void setSecondComponent(final ComplexKeyEntity secondComponent) {
        this.secondComponent = secondComponent;
    }
}
