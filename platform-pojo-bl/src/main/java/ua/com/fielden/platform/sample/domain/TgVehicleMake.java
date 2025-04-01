package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(String.class)
@KeyTitle(TgVehicleMake.KEY_TITLE)
@MapEntityTo
@DescTitle(TgVehicleMake.DESC_TITLE)
@Ignore
@CompanionObject(ITgVehicleMake.class)
public class TgVehicleMake extends AbstractEntity<String> {

    public static final String KEY_TITLE = "Make";
    public static final String DESC_TITLE = "Description";

    @IsProperty
    @Title(value = "Non-persisted prop", desc = "Desc")
    private String npProp;

    @Observable
    public TgVehicleMake setNpProp(final String npProp) {
        this.npProp = npProp;
        return this;
    }

    public String getNpProp() {
        return npProp;
    }

    @IsProperty
    @Title(value = "Competitor", desc = "Competitor")
    private TgVehicleMake competitor;

    @Observable
    public TgVehicleMake setCompetitor(final TgVehicleMake competitor) {
        this.competitor = competitor;
        return this;
    }

    public TgVehicleMake getCompetitor() {
        return competitor;
    }
}
