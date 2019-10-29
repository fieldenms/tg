package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;

/** 
 * Abstract functional entity for committing centre configuration.
 * <p>
 * This action has ability to<br>
 * a) change title / desc of existing owned configuration (button EDIT);<br>
 * b) create configuration copy (saveAs) from current default / inherited configuration and provide title / desc for it (button SAVE);<br>
 * b) save changes for current owned configuration (button SAVE).
 * 
 * @author TG Team
 *
 */
@DescTitle(value = "Description", desc = "Centre configuration description.")
public abstract class AbstractCentreConfigCommitAction extends AbstractCentreConfigAction {
    
    @IsProperty
    @Title(value = "Title", desc = "Centre configuration title.")
    @BeforeChange(@Handler(CentreConfigCommitActionTitleValidator.class))
    private String title;
    
    @IsProperty
    @Title("Context Holder")
    private CentreContextHolder centreContextHolder;
    
    @IsProperty
    @Title(value = "Skip UI", desc = "Controls the requirement to show or to skip displaying of the associated entity master.")
    private boolean skipUi = false;
    
    @Observable
    public AbstractCentreConfigCommitAction setSkipUi(final boolean skipUi) {
        this.skipUi = skipUi;
        return this;
    }
    
    public boolean isSkipUi() {
        return skipUi;
    }
    
    @Observable
    public AbstractCentreConfigCommitAction setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }
    
    public CentreContextHolder getCentreContextHolder() {
        return centreContextHolder;
    }
    
    @Observable
    public AbstractCentreConfigCommitAction setTitle(final String title) {
        this.title = title;
        return this;
    }
    
    public String getTitle() {
        return title;
    }
    
}