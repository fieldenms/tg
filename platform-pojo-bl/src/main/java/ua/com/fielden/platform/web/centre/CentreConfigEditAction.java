package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
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
public class CentreConfigEditAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    public enum EditKind { COPY, EDIT, SAVE }
    
    public CentreConfigEditAction() {
        setKey(NO_KEY);
    }
    
    @IsProperty
    @Title(value = "Title", desc = "Centre configuration title.")
    @BeforeChange(@Handler(CentreConfigEditActionTitleValidator.class))
    private String title;
    
    @IsProperty
    @Title("Context Holder")
    private CentreContextHolder centreContextHolder;
    
    @IsProperty
    @Title("Edit Kind")
    private String editKind;
    
    /**
     * Property <code>skipUi</code> controls the requirement to show or to skip displaying of the associated with this entity Master.
     */
    @IsProperty
    private boolean skipUi = false;
    
    @IsProperty(Object.class)
    @Title(value = "Custom object", desc = "Custom object")
    private final Map<String, Object> customObject = new HashMap<>();
    
    @Observable
    protected CentreConfigEditAction setCustomObject(final Map<String, Object> customObject) {
        this.customObject.clear();
        this.customObject.putAll(customObject);
        return this;
    }
    
    public Map<String, Object> getCustomObject() {
        return Collections.unmodifiableMap(customObject);
    }
    
    @Observable
    public CentreConfigEditAction setSkipUi(final boolean skipUi) {
        this.skipUi = skipUi;
        return this;
    }
    
    public boolean isSkipUi() {
        return skipUi;
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