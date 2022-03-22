package ua.com.fielden.platform.processors.meta_model.test_entities;


import ua.com.fielden.platform.annotations.meta_model.GenerateMetaModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@GenerateMetaModel
public class House extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Area", desc = "House area.")
    private Integer area;

    @IsProperty
    @MapTo
    @Title(value = "Insurance", desc = "Insurance for this house.")
    private Insurance insurance;

    @Observable
    public House setInsurance(final Insurance insurance) {
        this.insurance = insurance;
        return this;
    }

    public Insurance getInsurance() {
        return insurance;
    }

    @Observable
    public House setInteger(final Integer area) {
        this.area = area;
        return this;
    }

    public Integer getInteger() {
        return area;
    }
}
