package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.annotation.*;

/// The entity that represents the property row in domain explorer tree.
///
@KeyTitle(value = "Property Title", desc = "The property title")
@DescTitle(value = "Property Description", desc = "The property description")
public class DomainPropertyTreeEntity extends DomainTreeEntity {

    @IsProperty
    @Title("Is Key?")
    private boolean isKey;

    @IsProperty
    @Title(value = "Key Order", desc = "Order of the key in composite entity")
    private Integer keyOrder;

    @IsProperty
    @Title("Is Required?")
    private boolean isRequired;

    @IsProperty
    @Title("Is Union or Component?")
    private boolean unionEntityOrComponentWithMoreThanOneAttribute;

    @Observable
    public DomainPropertyTreeEntity setUnionEntityOrComponentWithMoreThanOneAttribute(final boolean unionEntityOrComponentWithMoreThanOneAttribute) {
        this.unionEntityOrComponentWithMoreThanOneAttribute = unionEntityOrComponentWithMoreThanOneAttribute;
        return this;
    }

    public boolean isUnionEntityOrComponentWithMoreThanOneAttribute() {
        return unionEntityOrComponentWithMoreThanOneAttribute;
    }

    @Observable
    public DomainPropertyTreeEntity setIsRequired(final boolean isRequired) {
        this.isRequired = isRequired;
        return this;
    }

    public boolean getIsRequired() {
        return isRequired;
    }

    @Observable
    public DomainPropertyTreeEntity setKeyOrder(final Integer keyOrder) {
        this.keyOrder = keyOrder;
        return this;
    }

    public Integer getKeyOrder() {
        return keyOrder;
    }

    @Observable
    public DomainPropertyTreeEntity setIsKey(final boolean isKey) {
        this.isKey = isKey;
        return this;
    }

    public boolean getIsKey() {
        return isKey;
    }

    @Override
    @Observable
    public DomainPropertyTreeEntity setDesc(String desc) {
        super.setDesc(desc);
        return this;
    }

}
