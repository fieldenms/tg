package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.util.Date;

import ua.com.fielden.platform.criteria.enhanced.FirstParam;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

@KeyType(String.class)
public class LifecyclePropertiesUpdater extends AbstractEntity<String> {

    private static final long serialVersionUID = 3994943961155127833L;

    @IsProperty
    @Dependent("to")
    @FirstParam(secondParam = "to")
    @Required
    @AfterChange(LifecyclePropertyChange.class)
    private Date from;

    @IsProperty
    @Dependent("from")
    @SecondParam(firstParam = "from")
    @Required
    @AfterChange(LifecyclePropertyChange.class)
    private Date to;

    private ILifecycleDomainTreeManager ldtm;

    /**
     * Constructor for the entity factory from TG.
     */
    protected LifecyclePropertiesUpdater() {
    }

    public Date getFrom() {
        return from;
    }

    @Observable
    @LeProperty("to")
    @NotNull
    public void setFrom(final Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    @Observable
    @GeProperty("from")
    @NotNull
    public void setTo(final Date to) {
        this.to = to;
    }

    public ILifecycleDomainTreeManager getLdtm(){
	return ldtm;
    }

    public void setLdtm(final ILifecycleDomainTreeManager ldtm){
	this.ldtm = ldtm;
	if(ldtm.getFrom() != null){
	    setFrom(ldtm.getFrom());
	}
	if(ldtm.getTo() != null){
	    setTo(ldtm.getTo());
	}
    }
}
