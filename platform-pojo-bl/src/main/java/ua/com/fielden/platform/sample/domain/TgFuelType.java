package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.BooleanParam;
import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.AuthorisationValidator;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanModify_guarded_Token;

import static ua.com.fielden.platform.entity.validation.AuthorisationValidator.PARAM_PERSISTED_ONLY;
import static ua.com.fielden.platform.entity.validation.AuthorisationValidator.PARAM_SECURITY_TOKEN;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@CompanionObject(ITgFuelType.class)
public class TgFuelType extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Guarded (persisted)", desc = "A property that requires authorisation, but only for a persisted entity.")
    @BeforeChange(@Handler(value = AuthorisationValidator.class, clazz = @ClassParam(name = PARAM_SECURITY_TOKEN, value = TgFuelType_CanModify_guarded_Token.class)))
    private String guardedIfPersisted;

    @IsProperty
    @MapTo
    @Title(value = "Guarded", desc = "A property that requires authorisation even if entity was not yet persisted.")
    @BeforeChange(@Handler(value = AuthorisationValidator.class, clazz = @ClassParam(name = PARAM_SECURITY_TOKEN, value = TgFuelType_CanModify_guarded_Token.class),
                                                                 bool = @BooleanParam(name = PARAM_PERSISTED_ONLY, value = false)))
    private String guardedEvenIfNotPersisted;

    public String getGuardedEvenIfNotPersisted() {
        return guardedEvenIfNotPersisted;
    }

    @Observable
    public TgFuelType setGuardedEvenIfNotPersisted(final String guardedEvenIfNotPersisted) {
        this.guardedEvenIfNotPersisted = guardedEvenIfNotPersisted;
        return this;
    }

    public String getGuardedIfPersisted() {
        return guardedIfPersisted;
    }

    @Observable
    public TgFuelType setGuardedIfPersisted(final String guardedIfPersisted) {
        this.guardedIfPersisted = guardedIfPersisted;
        return this;
    }

}
