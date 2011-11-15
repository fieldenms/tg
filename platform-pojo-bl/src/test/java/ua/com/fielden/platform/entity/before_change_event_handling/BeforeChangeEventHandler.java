package ua.com.fielden.platform.entity.before_change_event_handling;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * BCE event handler for testing purposes.
 * @author TG Team
 *
 */
public class BeforeChangeEventHandler implements IBeforeChangeEventHandler<Object> {

    private String strParam;
    private int intParam1;
    private int intParam2;
    private double dblParam;
    private Money moneyParam;
    private Date dateParam;
    private DateTime dateTimeParam;
    private Controller controllerParam;
    private Class<?> classParam;

    private boolean invoked = false;

    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	if (controllerParam != null) {
	    controllerParam.run();
	}

	setInvoked(true);
	return Result.successful(null);
    }

    public String getStrParam() {
        return strParam;
    }

    public void setStrParam(final String strParam) {
        this.strParam = strParam;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(final int intParam1) {
        this.intParam1 = intParam1;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(final int intParam2) {
        this.intParam2 = intParam2;
    }

    public double getDblParam() {
        return dblParam;
    }

    public void setDblParam(final double dblParam) {
        this.dblParam = dblParam;
    }

    public Money getMoneyParam() {
        return moneyParam;
    }

    public void setMoneyParam(final Money moneyParam) {
        this.moneyParam = moneyParam;
    }

    public Date getDateParam() {
        return dateParam;
    }

    public void setDateParam(final Date dateParam) {
        this.dateParam = dateParam;
    }

    public DateTime getDateTimeParam() {
        return dateTimeParam;
    }

    public void setDateTimeParam(final DateTime dateTimeParam) {
        this.dateTimeParam = dateTimeParam;
    }

    public Controller getControllerParam() {
        return controllerParam;
    }

    public void setControllerParam(final Controller controllerParam) {
        this.controllerParam = controllerParam;
    }

    public boolean isInvoked() {
        return invoked;
    }

    public void setInvoked(final boolean invoked) {
        this.invoked = invoked;
    }

    public Class<?> getClassParam() {
        return classParam;
    }

    public void setClassParam(final Class<?> classParam) {
        this.classParam = classParam;
    }

}
