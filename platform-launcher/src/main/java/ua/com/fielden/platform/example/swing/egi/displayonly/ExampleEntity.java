package ua.com.fielden.platform.example.swing.egi.displayonly;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle("Example entity type")
@KeyType(String.class)
@KeyTitle(value = "Example entity", desc = "Example entity description")
@DescTitle(value = "Description", desc = "Example entity description")
public class ExampleEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 6832859161904395916L;

    @IsProperty
    @Title(value = "String property", desc = "String property description")
    private String stringProperty;

    @IsProperty
    @Title(value = "Init. date", desc = "Date of initiation")
    private Date initDate;

    @IsProperty
    @Title(value = "active", desc = "determines the activity of simple entity.")
    private boolean active = false;

    @IsProperty
    @Title(value = "Num. value", desc = "Number value ")
    private Integer numValue;

    @Title(value = "Nested entity", desc = "Nested entity description")
    @IsProperty
    private ExampleEntity nestedEntity;

    public String getStringProperty() {
        return stringProperty;
    }

    @Observable
    public ExampleEntity setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
        return this;
    }

    public Date getInitDate() {
        return initDate;
    }

    @Observable
    public ExampleEntity setInitDate(final Date initDate) {
        this.initDate = initDate;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    @Observable
    public ExampleEntity setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public Integer getNumValue() {
        return numValue;
    }

    @Observable
    public ExampleEntity setNumValue(final Integer numValue) {
        this.numValue = numValue;
        return this;
    }

    public ExampleEntity getNestedEntity() {
        return nestedEntity;
    }

    @Observable
    public ExampleEntity setNestedEntity(final ExampleEntity nestedEntity) {
        this.nestedEntity = nestedEntity;
        return this;
    }

    @Override
    @Observable
    public ExampleEntity setDesc(final String desc) {
        super.setDesc(desc);
        return this;
    }
}
