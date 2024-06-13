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
@CompanionObject(TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessorCo.class)
public class TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor extends AbstractTgWebApiEntityForWebApiPropertyDataFetcherTest {

    @IsProperty
    @MapTo
    private boolean prop;

    @Observable
    public TgWebApiEntityWithBooleanPropWithGetGetterAndRecordLikeAccessor setProp(final boolean value) {
        prop = value;
        return this;
    }

    /**
     * "Non-canonical" TG getter for {@code boolean}-typed property.
     */
    public boolean getProp() {
        return prop;
    }

    /**
     * Alternative record-like accessor for {@code boolean}-typed property. Returns wrong, opposite, value.
     */
    public boolean prop() {
        return !getProp();
    }

}