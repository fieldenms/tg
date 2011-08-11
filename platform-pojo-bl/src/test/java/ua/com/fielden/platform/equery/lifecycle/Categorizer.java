package ua.com.fielden.platform.equery.lifecycle;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.types.ICategorizer;
import ua.com.fielden.platform.types.ICategory;

/**
 * Demo/test Categorizer for {@link Vehicle} "status" property.
 *
 * @author Jhou
 *
 */
public class Categorizer implements ICategorizer {
    public Categorizer() {
	super();
    }

    @Override
    public ICategory getCategory(final Object value) {
	return ("Value 1".equals(value) || "Value 2".equals(value)) ? Category.AVAILABLE : //
		(("Value 3".equals(value)) ? Category.BROKEN : //
			(("Value 4".equals(value)) ? Category.UNOPERATIONAL : //
				Category.UNCATEGORIZED));
    }

    @Override
    public List<? extends ICategory> getAllCategories() {
	return Arrays.asList(Category.AVAILABLE, Category.BROKEN, Category.UNOPERATIONAL);
    }

    @Override
    public List<? extends ICategory> getMainCategories() {
	return Arrays.asList(Category.AVAILABLE, Category.BROKEN);
    }

    /**
     * Categories demo/test implementation.
     *
     * @author Jhou
     *
     */
    private static enum Category implements ICategory {
	AVAILABLE("Available", "Smth is available (Value 1, Value 2)", Color.BLUE), BROKEN("Broken", "Smth is broken (Value 3)", Color.YELLOW), UNOPERATIONAL("Unoperational",
		"Smth is completely un-operational(Value 4)", Color.RED), UNCATEGORIZED("Non-categorized", "Non-categorized", null);

	private final String name;
	private final String desc;
	private final Color color;

	private Category(final String name, final String desc, final Color color) {
	    this.name = name;
	    this.desc = desc;
	    this.color = color;
	}

	@Override
	public boolean isNormal() {
	    return AVAILABLE.equals(this);
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
	    return name;
	}

	@Override
	public String toString() {
	    return name;
	}

    }

    @Override
    public List<String> getDistributionProperties() {
        return Collections.emptyList();
    }

}