package ua.com.fielden.platform.entity.functional.master;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IAcknowledgeWarnings.class)
@EntityTitle(value = "Acknowledge warnings", desc = "Acknowledge warnings of the current initiating entity")
public class AcknowledgeWarnings extends AbstractFunctionalEntityWithCentreContext<String> implements IContinuationData {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(PropertyWarning.class)
    @Title(value = "Warnings", desc = "A list of user property warnings")
    private Set<PropertyWarning> warnings = new LinkedHashSet<PropertyWarning>();

    @Observable
    protected AcknowledgeWarnings setWarnings(final Set<PropertyWarning> warnings) {
        this.warnings.clear();
        this.warnings.addAll(warnings);
        return this;
    }

    public Set<PropertyWarning> getWarnings() {
        return Collections.unmodifiableSet(warnings);
    }
}