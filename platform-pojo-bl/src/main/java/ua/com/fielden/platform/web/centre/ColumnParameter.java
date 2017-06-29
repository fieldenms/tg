package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle("Column parameter")
@CompanionObject(IColumnParameter.class)
public class ColumnParameter extends AbstractEntity<String> {

    @IsProperty
    @Title("Property name")
    private String propName;

    @IsProperty
    @Title("Column width")
    private Integer width;

    @IsProperty
    @Title("Column grow factor")
    private Integer growFactor;

    @Observable
    public ColumnParameter setGrowFactor(final Integer growFactor) {
        this.growFactor = growFactor;
        return this;
    }

    public Integer getGrowFactor() {
        return growFactor;
    }

    @Observable
    public ColumnParameter setWidth(final Integer width) {
        this.width = width;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    @Observable
    public ColumnParameter setPropName(final String propName) {
        this.propName = propName;
        return this;
    }

    public String getPropName() {
        return propName;
    }

}