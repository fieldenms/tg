package ua.com.fielden.platform.egi;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle("Columns Key Property")
@DescTitle("Columns Description")
@CompanionObject(IPropertyColumn.class)
public class PropertyColumn extends AbstractEntity<String> {

    @IsProperty
    @Title("Value Property")
    private String valueProp;

    @IsProperty
    @Title("Tooltip Property")
    private String tooltipProp;

    @IsProperty
    @Title("Column Title")
    private String title;

    @IsProperty
    @Title("Width")
    private Integer width;

    @IsProperty
    @Title("Minimal Width")
    private Integer minWidth;

    @Observable
    public PropertyColumn setMinWidth(final Integer minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    public Integer getMinWidth() {
        return minWidth;
    }

    @Observable
    public PropertyColumn setWidth(final Integer width) {
        this.width = width;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    @Observable
    public PropertyColumn setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    @Observable
    public PropertyColumn setTooltipProp(final String tooltipProp) {
        this.tooltipProp = tooltipProp;
        return this;
    }

    public String getTooltipProp() {
        return tooltipProp;
    }

    @Observable
    public PropertyColumn setValueProp(final String valueProp) {
        this.valueProp = valueProp;
        return this;
    }

    public String getValueProp() {
        return valueProp;
    }
}
