package ua.com.fielden.platform.sample.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.TransactionEntity;

@KeyType(String.class)
@KeyTitle(value = "Гео-зона", desc = "Унікальна назва геозони, що задає її в контексті всіх геозон.")
@EntityTitle(value = "Гео-зона", desc = "Полігон, що задає певну зону, через яку проїжджають транспортні засоби.")
@CompanionObject(ITgPolygon.class)
@MapEntityTo
@DescTitle(value = "Опис", desc = "Опис")
@TransactionEntity("DOES NOT MATTER")
public class TgPolygon extends AbstractEntity<String> {
    @IsProperty(TgCoordinate.class)
    @Title(value = "Координати", desc = "Координати полігона")
    // FIXME This collection should be definitely sorted by natural ordering: TgCoordinate.order ASC.
    // FIXME It ensures a correct order of coordinates while fetching them in one couple with a TgPolygon.
    private final Set<TgCoordinate> coordinates = new HashSet<TgCoordinate>();

    @Observable
    protected TgPolygon setCoordinates(final Set<TgCoordinate> coordinates) {
        this.coordinates.clear();
        this.coordinates.addAll(coordinates);
        return this;
    }

    public Set<TgCoordinate> getCoordinates() {
        return Collections.unmodifiableSet(coordinates);
    }
}