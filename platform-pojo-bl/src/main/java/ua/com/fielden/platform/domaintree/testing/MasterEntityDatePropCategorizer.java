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

	return (value == null ? Category.UNCATEGORIZED : //
		((now.getTime() > date.getTime() ? Category.FUTURE : //
		(((now.getTime() == date.getTime()) ? Category.NOW : //
			(((now.getTime() < date.getTime()) ? Category.PAST : //
				Category.UNCATEGORIZED)))))));
    }

    @Override
    public List<? extends ICategory> getAllCategories() {
	return Arrays.asList(Category.FUTURE, Category.NOW, Category.PAST);
    }

    @Override
    public List<? extends ICategory> getMainCategories() {
	return Arrays.asList(Category.FUTURE, Category.PAST);
    }

    /**
     * Categories demo/test implementation.
     *
     * @author Tg Team
     *
     */
    private static enum Category implements ICategory {
	FUTURE("Future", "The date is in future", Color.BLUE), //
	NOW("Now", "The date is Now", Color.YELLOW), //
	PAST("Past", "The date is in the past", Color.RED), //
	UNCATEGORIZED("Non-categorized", "Non-categorized", null);

	private final String title;
	private final String desc;
	private final Color color;

	private Category(final String title, final String desc, final Color color) {
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