package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Functional entity for copying centre configuration.
 * <p>
 * Key of this functional entity represents the name of configuration to be copied or empty string for the case of unnamed configuration.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ICentreConfigCopyAction.class)
@DescTitle("Description")
@DescRequired
public class CentreConfigCopyAction extends AbstractFunctionalEntityWithCentreContext<String> {
    
    @IsProperty
    @Title(value = "Title", desc = "Title for centre configuration copy.")
    @Required
    // TODO @BeforeChange(CentreConfigCopyActionTitleValidator.class)
    private String title;
    
    @Observable
    public CentreConfigCopyAction setTitle(final String title) {
        this.title = title;
        return this;
    }
    
    public String getTitle() {
        return title;
    }
    
}