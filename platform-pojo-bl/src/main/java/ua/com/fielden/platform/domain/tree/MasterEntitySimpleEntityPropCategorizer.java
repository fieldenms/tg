package ua.com.fielden.platform.domain.tree;

import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.types.ICategorizer;
import ua.com.fielden.platform.types.ICategory;

/**
 * Testing categorizer for {@link MasterEntity} "simpleEntityProp" property.
 *
 * @author TG Team
 *
 */
public class MasterEntitySimpleEntityPropCategorizer implements ICategorizer {

    public MasterEntitySimpleEntityPropCategorizer() {
	super();
    }

    @Override
    public ICategory getCategory(final Object value) {
	return null;
    }

    @Override
    public List<? extends ICategory> getAllCategories() {
	return Arrays.asList();
    }

    @Override
    public List<? extends ICategory> getMainCategories() {
	return Arrays.asList();
    }

    @Override
    public List<String> getDistributionProperties() {
	return Arrays.asList();
    }
}
