package ua.com.fielden.platform.entity.functional.master;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * An entity representing warnings that may occur during editing of entities and need to be explicitly acknowledged by users.
 * 
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(IAcknowledgeWarnings.class)
@EntityTitle(value = "Acknowledge warnings", desc = "Acknowledge warnings of the current initiating entity")
public class AcknowledgeWarnings extends AbstractFunctionalEntityWithCentreContext<NoKey> implements IContinuationData {
    
    @IsProperty(PropertyWarning.class)
    @Title(value = "Warnings", desc = "A list of user property warnings")
    private Set<PropertyWarning> warnings = new LinkedHashSet<PropertyWarning>();

    public AcknowledgeWarnings() {
        setKey(NO_KEY);
    }
    
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