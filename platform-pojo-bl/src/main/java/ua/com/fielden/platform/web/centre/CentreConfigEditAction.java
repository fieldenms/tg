package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;

/** 
 * Functional entity for editing / copying centre configuration.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ICentreConfigEditAction.class)
@KeyType(NoKey.class)
@DescTitle(value = "Description", desc = "Centre configuration description.")
@DescRequired
public class CentreConfigEditAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    public enum EditKind { COPY, EDIT }
    
    public CentreConfigEditAction() {
        setKey(NO_KEY);
    }
    
    @IsProperty
    @Title(value = "Title", desc = "Centre configuration title.")
    @Required
    @BeforeChange(@Handler(CentreConfigEditActionTitleValidator.class))
    private String title;
    
    @IsProperty
    @Title("Context Holder")
    private CentreContextHolder centreContextHolder;
    
    @IsProperty
    @Title("Edit Kind")
    private String editKind;
    
    @IsProperty
    @Title(value = "Is preferred?", desc = "Indicates whether this configuration is preferred over the others.")
    private boolean preferred = false;
    
    public boolean isPreferred() {
        return preferred;
    }
    
    @Observable
    public CentreConfigEditAction setPreferred(final boolean value) {
        preferred = value;
        return this;
    }
    
    @Observable
    public CentreConfigEditAction setEditKind(final String editKind) {
        this.editKind = editKind;
        return this;
    }
    
    public String getEditKind() {
        return editKind;
    }
    
    @Observable
    public CentreConfigEditAction setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }
    
    public CentreContextHolder getCentreContextHolder() {
        return centreContextHolder;
    }
    
    @Observable
    public CentreConfigEditAction setTitle(final String title) {
        this.title = title;
        return this;
    }
    
    public String getTitle() {
        return title;
    }
    
}