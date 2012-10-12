package ua.com.fielden.platform.domaintree.testing;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.types.ICategorizer;
import ua.com.fielden.platform.types.ICategory;

/**
 * Testing categorizer for {@link MasterEntity} "dateProp" property.
 *
 * @author TG Team
 *
 */
public class MasterEntityDatePropCategorizer implements ICategorizer {
    public MasterEntityDatePropCategorizer() {
	super();
    }

    @Override
    public ICategory getCategory(final Object value) {
	final Date date = (Date) value;
	final Date now = new Date();

	return (value == null ? MasterEntityDatePropCategory.UNCATEGORIZED : //
		((now.getTime() > date.getTime() ? MasterEntityDatePropCategory.FUTURE : //
		(((now.getTime() == date.getTime()) ? MasterEntityDatePropCategory.NOW : //
			(((now.getTime() < date.getTime()) ? MasterEntityDatePropCategory.PAST : //
				MasterEntityDatePropCategory.UNCATEGORIZED)))))));
    }

    @Override
    public List<? extends ICategory> getAllCategories() {
	return Arrays.asList(MasterEntityDatePropCategory.FUTURE, MasterEntityDatePropCategory.NOW, MasterEntityDatePropCategory.PAST);
    }

    @Override
    public List<? extends ICategory> getMainCategories() {
	return Arrays.asList(MasterEntityDatePropCategory.FUTURE, MasterEntityDatePropCategory.PAST);
    }

    /**
     * Categories demo/test implementation.
     *
     * @author Tg Team
     *
     */
    public static enum MasterEntityDatePropCategory implements ICategory {
	FUTURE("Future", "The date is in future", Color.BLUE), //
	NOW("Now", "The date is Now", Color.YELLOW), //
	PAST("Past", "The date is in the past", Color.RED), //
	UNCATEGORIZED("Non-categorized", "Non-categorized", null);

	private final String title;
	private final String desc;
	private final Color color;

	private MasterEntityDatePropCategory(final String title, final String desc, final Color color) {
	    this.title = title;
	    this.desc = desc;
	    this.color = color;
	}

	@Override
	public boolean isNormal() {
	    return PAST.equals(this);
	}

	@Override
	public String getDesc() {
	    return desc;
	}

	@Override
	public Color getColor() {
	    return color;
	}

	@Override
	public boolean isUncategorized() {
	    return UNCATEGORIZED.equals(this);
	}

	@Override
	public String getName() {
	    return title;
	}

	@Override
	public String toString() {
	    return title;
	}
    }

    @Override
    public List<String> getDistributionProperties() {
        return Collections.emptyList();
    }
}