package ua.com.fielden.platform.entity_centre.review.criteria;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.interception.AccessToken;
import ua.com.fielden.platform.security.interception.NoAccessToken;
import ua.com.fielden.platform.types.Money;

@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(String.class)
public class AuthorisationTestEntity extends AbstractEntity<String> {

    @IsProperty
    @Authorise(AccessToken.class)
    private Money authorisedProp;

    @IsProperty
    @Authorise(NoAccessToken.class)
    private Money unauthorisedProp;

    public Money getUnauthorisedProp() {
        return unauthorisedProp;
    }

    @Observable
    public AuthorisationTestEntity setUnauthorisedProp(final Money unauthorisedProp) {
        this.unauthorisedProp = unauthorisedProp;
        return this;
    }

    public Money getAuthorisedProp() {
        return authorisedProp;
    }

    @Observable
    public AuthorisationTestEntity setAuthorisedProp(final Money authorisedProp) {
        this.authorisedProp = authorisedProp;
        return this;
    }
}
