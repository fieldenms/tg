package ua.com.fielden.platform.swing.components.bind.test;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.swing.components.bind.test.Entity.Strategy;
import ua.com.fielden.platform.types.Money;

/**
 * This factory provides domain validators for Entity class properties.
 *
 * @author jhou
 *
 */
public class EntityValidatorFactory {

    /**
     * Domain validator for Entity property "date"
     *
     * @author jhou
     *
     */
    public class EntityDateValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final Date date = (Date) newValue;
	    if (date != null && new Date().before(date)) {
		return new Result(this, new Exception("date cannot be specified as #date in future# :) "));
	    }
	    return new Result(property.getEntity(), "Date property value is correct.");
	}

    }

    /**
     * Domain validator for Entity property "bigDecimal"
     *
     * @author jhou
     *
     */
    public class EntityBigDecimalValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final BigDecimal bigDecimal = (BigDecimal) newValue;
	    if (bigDecimal.doubleValue() > 1000000) {
		return new Result(this, new Exception("double value have to be < 1000000"));
	    }
	    return new Result(property.getEntity(), "BigDecimal property value is correct.");
	}

    }

    /**
     * Domain validator for Entity property "money"
     *
     * @author jhou
     *
     */
    public class EntityMoneyValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final Money money = (Money) newValue;
	    if (money.getAmount().doubleValue() > 1000000) {
		return new Result(this, new Exception("money amount have to be < 1000000"));
	    }
	    return new Result(property.getEntity(), "Money amount is < 1000000. property value is correct.");
	}

    }

    /**
     * Domain validator for Entity property "bool"
     *
     * @author jhou
     *
     */
    public class EntityBoolValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final Object entity = property.getEntity();
	    if (newValue.equals(false)) {
		return new Result(entity, new Exception("false value cannot be set"));
	    }
	    return new Result(entity, "Bool property value is correct.");
	}

    }

    /**
     * Domain validator for Entity property "demoEntity"
     *
     * @author jhou
     *
     */
    public class EntityDemoEntityValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final Entity ent = (Entity) property.getEntity();
	    ;
	    if (Strategy.REVERT.equals(ent.getStrategy())) {
		return new Result(this, new Exception("demoEntity " + newValue + "(if exists) cannot be set when Strategy==REVERT"));
	    }
	    return new Result(ent, "demoEntity property value is correct.");
	}

    }

    /**
     * Domain validator for Entity property "list"
     *
     * @author jhou
     *
     */
    public class EntityListValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    if (((ArrayList<?>) newValue).size() == 0) {
		return new Result(this, new Exception("you cannot set the empty list"));
	    }
	    return new Result(property.getEntity(), "list property value is correct.");
	}

    }

    /**
     * Domain validator for Entity property "list"
     *
     * @author jhou
     *
     */
    public class EntityBicyclesValidator implements IValidator {

	@Override
	@SuppressWarnings("unchecked")
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final Result incorrect = new Result(this, new Exception("Collection should not be empty:) "));
	    final Result correct = new Result(property.getEntity(), "list property value is correct.");

	    return correct;
	    // TODO the following logic needs to be re-thought due to removal of the mutator parameter.

	    /*switch (Mutator.getValueByMethod(mutator)) {
	    case SETTER: {
	    return ((Collection) newValue).isEmpty() ? incorrect : correct;
	    }
	    case INCREMENTOR: {
	    return correct;
	    }
	    case DECREMENTOR: {
	    final Collection collection = ((Collection<?>) ((AbstractEntity<?>) property.getEntity()).get(property.getName()));
	    return (collection.contains(oldValue) && collection.size() == 1) ? incorrect : correct;
	    }
	    default: {
	    throw new IllegalStateException("Incorrect mutator type");
	    }
	    }*/
	}
    }

    /**
     * Domain validator for Entity property "number"
     *
     * @author jhou
     *
     */
    public class EntityNumberValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final Entity ent = (Entity) property.getEntity();
	    final Integer number = (Integer) newValue;
	    //	    if (ent.getBool() && number >= 300) {
	    //		return new Result(this, new Exception("can not set the " + number + " value >= 300 when the checkBox is checked (and commited)"));
	    //	    }
	    if (new Integer(77).equals(number)) {
		return new Warning(this, "Domain validation warning : The value " + number + " is dangerous.");
	    }
	    return new Result(ent, "Number (integer) property value is correct.");
	}

    }

    /**
     * Domain validator for Entity property "strategy"
     *
     * @author jhou
     *
     */
    public class EntityStrategyValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final Object entity = property.getEntity();
	    if (newValue.equals(Strategy.REVERT_ON_INVALID)) {
		return new Result(entity, new Exception("REVERT_ON_INVALID value cannot be set"));
	    }
	    return new Result(entity, "Strategy property value is correct.");
	}

    }

    /**
     * Domain validator for Entity property "string"
     *
     * @author jhou
     *
     */
    public class EntityStringValidator implements IValidator {

	@Override
	public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	    final String string = (String) newValue;

	    if (string.length() > 20) {
		return new Result(this, new Exception("can not set the " + string + " value that have length() > 20"));
	    }
	    return new Result(property.getEntity(), "String property value is correct.");
	}

    }

}
