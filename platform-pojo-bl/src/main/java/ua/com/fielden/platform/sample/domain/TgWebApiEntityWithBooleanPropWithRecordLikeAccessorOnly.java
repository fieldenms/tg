package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Entity for {@link IWebApi} testing.
 *
 * @author TG Team
 *
 */
@MapEntityTo
@CompanionObject(TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnlyCo.class)
public class TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly extends AbstractTgWebApiEntityForWebApiPropertyDataFetcherTest {

    @IsProperty
    @MapTo
    private boolean prop;

    @Observable
    public TgWebApiEntityWithBooleanPropWithRecordLikeAccessorOnly setProp(final boolean value) {
        prop = value;
        return this;
    }

    /**
     * Alternative record-like accessor for {@code boolean}-typed property.
     */
    public boolean prop() {
        return prop;
    }

    //-----to-be-removed-----
    public boolean isProp() {
        return prop;
    }

}