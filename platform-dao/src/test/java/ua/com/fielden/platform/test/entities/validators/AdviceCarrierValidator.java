package ua.com.fielden.platform.test.entities.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test.domain.entities.Advice;
import ua.com.fielden.platform.test.domain.entities.Wagon;

/**
 * Domain validator for property <code>carrier</code> ({@link Advice#setCarrier(ua.com.fielden.pnl.equipment.Wagon)}).
 *
 * @author 01es
 *
 */
public class AdviceCarrierValidator implements IBeforeChangeEventHandler<Wagon> {

    @Override
    public Result handle(final MetaProperty<Wagon> property, final Wagon newValue, final Wagon oldValue, final Set<Annotation> mutatorAnnotations) {
        // TODO Auto-generated method stub
        return null;
    }
    //    private final IAdviceDao dao;
    //
    //    @Inject
    //    protected AdviceCarrierValidator(final IAdviceDao dao) {
    //	this.dao = dao;
    //    }
    //
    //    @Override
    //    public Result handle(final MetaProperty property, final Wagon newValue, final Wagon oldValue, final Set<Annotation> mutatorAnnotations) {
    //	final Advice advice = (Advice) property.getEntity();
    //	// validate if it is at all possible to set carrier
    //	if (advice.isDispatched()) {
    //	    if ((advice.getCarrier() != null && !advice.getCarrier().equals(newValue)) || //
    //		    (advice.getCarrier() == null && newValue != null)) {
    //		return new Result(advice, new IllegalStateException("Carrier cannot be changed once advice is dispatched."));
    //	    }
    //	}
    //	// newValue cannot be null? HAVE TO use NotNull validator before!
    //	if (newValue != null && newValue.equals(oldValue)) {
    //	    return new Result(advice, "Carrier property value is correct.");
    //	}
    //	// validate if the carrier is not currently used on another not received advice
    //	if (newValue != null) {
    //	    final IQueryOrderedModel<Advice> q = select(Advice.class)//
    //	    .where().prop("received").isFalse().and().prop("carrier").eq().val(newValue).model();
    //	    final List<Advice> advices = dao.getPage(q, 0, 100).data();
    //	    if (advices.size() > 0) {
    //		return new Result(advice, new IllegalStateException("This carrier is currently used by advice " + advices.get(0).getKey() + "."));
    //	    }
    //	}
    //	return new Result(advice, "Carrier property value is correct.");
    //    }

}
