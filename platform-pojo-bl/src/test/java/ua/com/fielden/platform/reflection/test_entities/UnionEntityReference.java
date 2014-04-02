package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

public class UnionEntityReference extends AbstractUnionEntity {

    private static final long serialVersionUID = 3836447714349644197L;

    @IsProperty
    @Title(value = "first reference", desc = "first reference description")
    private FirstReferencedClass firstReference;

    @IsProperty
    @Title(value = "second reference", desc = "second reference description")
    private SecondReferencedClass secondReference;

    public FirstReferencedClass getFirstReference() {
        return firstReference;
    }

    @Observable
    public void setFirstReference(final FirstReferencedClass firstReference) {
        this.firstReference = firstReference;
    }

    public SecondReferencedClass getSecondReference() {
        return secondReference;
    }

    @Observable
    public void setSecondReference(final SecondReferencedClass secondReference) {
        this.secondReference = secondReference;
    }

}
