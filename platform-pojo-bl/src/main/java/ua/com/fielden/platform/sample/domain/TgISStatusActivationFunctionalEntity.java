package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An example functional entity to activate the IS status.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgISStatusActivationFunctionalEntity.class)
public class TgISStatusActivationFunctionalEntity extends AbstractFunctionalEntityWithCentreContext<String> {
    
    @IsProperty
    @Title("Selected Entity Id")
    private Long selectedEntityId;

    @Observable
    public TgISStatusActivationFunctionalEntity setSelectedEntityId(final Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
        return this;
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }
    
}