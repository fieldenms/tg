package ua.com.fielden.platform.web.centre;

import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;

/** 
 * Abstract functional entity for editing / saving centre configuration.
 * 
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@DescTitle(value = "Description", desc = "Centre configuration description.")
public abstract class AbstractCentreConfigEditAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    public AbstractCentreConfigEditAction() {
        setKey(NO_KEY);
    }
    
    @IsProperty
    @Title(value = "Title", desc = "Centre configuration title.")
    @BeforeChange(@Handler(CentreConfigEditActionTitleValidator.class))
    private String title;
    
    @IsProperty
    @Title("Context Holder")
    private CentreContextHolder centreContextHolder;
    
    /**
     * Property <code>skipUi</code> controls the requirement to show or to skip displaying of the associated with this entity Master.
     */
    @IsProperty
    private boolean skipUi = false;
    
    @IsProperty(Object.class)
    @Title("Custom object")
    private final Map<String, Object> customObject = new HashMap<>();
    
    @Observable
    protected AbstractCentreConfigEditAction setCustomObject(final Map<String, Object> customObject) {
        this.customObject.clear();
        this.customObject.putAll(customObject);
        return this;
    }
    
    public Map<String, Object> getCustomObject() {
        return unmodifiableMap(customObject);
    }
    
    @Observable
    public AbstractCentreConfigEditAction setSkipUi(final boolean skipUi) {
        this.skipUi = skipUi;
        return this;
    }
    
    public boolean isSkipUi() {
        return skipUi;
    }
    
    @Observable
    public AbstractCentreConfigEditAction setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }
    
    public CentreContextHolder getCentreContextHolder() {
        return centreContextHolder;
    }
    
    @Observable
    public AbstractCentreConfigEditAction setTitle(final String title) {
        this.title = title;
        return this;
    }
    
    public String getTitle() {
        return title;
    }
    
}