package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Координата", desc = "Координата, що належить деякому полігону.")
@EntityTitle(value = "Координата", desc = "Координата, що належить деякому полігону.")
@CompanionObject(ITgCoordinate.class)
@MapEntityTo
@DescTitle(value = "Опис", desc = "Опис")
public class TgCoordinate extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @Title("Полігон")
    @MapTo
    @CompositeKeyMember(1)
    private TgPolygon polygon;

    @IsProperty
    @MapTo
    @Title(value = "Номер", desc = "Номер координати в полігоні")
    @CompositeKeyMember(2)
    private Integer order;

    @IsProperty
    @MapTo(value = "x_", precision = 18, scale = 10)
    @Title(value = "X-координата", desc = "Значення довготи")
    private BigDecimal longitude;

    @IsProperty
    @MapTo(value = "y_", precision = 18, scale = 10)
    @Title(value = "Y-координата", desc = "Значення широти")
    private BigDecimal latitude;

    @Observable
    public TgCoordinate setOrder(final Integer name) {
        this.order = name;
        return this;
    }

    public Integer getOrder() {
        return order;
    }

    @Observable
    public TgCoordinate setLatitude(final BigDecimal latitude) {
        this.latitude = latitude;
        return this;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    @Observable
    public TgCoordinate setLongitude(final BigDecimal longitude) {
        this.longitude = longitude;
        return this;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    @Observable
    public TgCoordinate setPolygon(final TgPolygon value) {
        this.polygon = value;
        return this;
    }

    public TgPolygon getPolygon() {
        return polygon;
    }

}