package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Abstract entity for all {@link IWebApi} default property data fetching tests.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@MapEntityTo
public abstract class AbstractTgWebApiEntityForWebApiPropertyDataFetcherTest extends AbstractEntity<String> {

    // additional prop to differentiate proxied and full entities ('key' is not enough, because it always gets fetched implicitly)

    @IsProperty
    @MapTo
    private String otherProp;

    @Observable
    public AbstractTgWebApiEntityForWebApiPropertyDataFetcherTest setOtherProp(final String value) {
        otherProp = value;
        return this;
    }

    public String getOtherProp() {
        return otherProp;
    }

}