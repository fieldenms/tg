package ua.com.fielden.platform.sample.domain.compound;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;

/** 
 * Locator entity object.
 * 
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(ITgCompoundEntityLocator.class)
public class TgCompoundEntityLocator extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public TgCompoundEntityLocator() {
        setKey(NO_KEY);
    }

    @IsProperty
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private TgCompoundEntity tgCompoundEntity;
    
    @Observable
    public TgCompoundEntityLocator setTgCompoundEntity(final TgCompoundEntity value) {
        this.tgCompoundEntity = value;
        return this;
    }
    
    public TgCompoundEntity getTgCompoundEntity() {
        return tgCompoundEntity;
    }
    
}