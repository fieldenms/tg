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
@CompanionObject(TgWebApiEntityWithGetGetterAndRecordLikeAccessorCo.class)
public class TgWebApiEntityWithGetGetterAndRecordLikeAccessor extends AbstractTgWebApiEntityForWebApiPropertyDataFetcherTest {

    @IsProperty
    @MapTo
    private String prop;

    @Observable
    public TgWebApiEntityWithGetGetterAndRecordLikeAccessor setProp(final String value) {
        prop = value;
        return this;
    }

    /**
     * "Canonical" TG getter for {@code String}-typed property.
     */
    public String getProp() {
        return prop;
    }

    /**
     * Alternative record-like accessor for {@code String}-typed property. Returns wrong, twice concatenated, value for testing purposes.
     */
    public String prop() {
        return getProp() + getProp();
    }

}