package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.UpperCase;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgCreatePersistentStatusAction.class)
@DescRequired
@DescTitle(value = "Desc", desc = "Description of the status being created.")
public class TgCreatePersistentStatusAction extends AbstractFunctionalEntityWithCentreContext<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Status Code", desc = "New and unique satus code.")
    @CompositeKeyMember(1)
    @UpperCase
    private String statusCode;

    @IsProperty
    @Title(value = "Chosen property", desc = "A master entity property that is related to this action instance.")
    private String actionProperty;

    @IsProperty
    @MapTo
    @Title(value = "Status", desc = "A status entity that has been created by this action.")
    @SkipEntityExistsValidation
    private TgPersistentStatus status;

    @Observable
    public TgCreatePersistentStatusAction setStatus(final TgPersistentStatus status) {
        this.status = status;
        return this;
    }

    public TgPersistentStatus getStatus() {
        return status;
    }

    @Observable
    public TgCreatePersistentStatusAction setActionProperty(final String actionProperty) {
        this.actionProperty = actionProperty;
        return this;
    }

    public String getActionProperty() {
        return actionProperty;
    }
    
    @Observable
    public TgCreatePersistentStatusAction setStatusCode(final String status) {
        this.statusCode = status;
        return this;
    }

    public String getStatusCode() {
        return statusCode;
    }
}