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
public class BeforeChangeEventHandler implements IBeforeChangeEventHandler {

    private String strParam;
    private int intParam1;
    private int intParam2;
    private double dblParam;
    private Money moneyParam;
    private Date dateParam;
    private DateTime dateTimeParam;
    private Controller controllerParam;

    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {

	System.out.println("str param: " + strParam);
	System.out.println("int param 1: " + intParam1);
	System.out.println("int param 2: " + intParam2);
	System.out.println("dbl param: " + dblParam);
	System.out.println("money param: " + moneyParam);
	System.out.println("date param: " + dateParam);
	System.out.println("date time param: " + dateTimeParam);
	if (controllerParam != null) {
	    controllerParam.run();
	}

	return Result.successful(null);
    }

}
