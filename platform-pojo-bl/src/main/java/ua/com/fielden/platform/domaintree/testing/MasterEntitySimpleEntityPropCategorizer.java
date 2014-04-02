package ua.com.fielden.platform.domaintree.testing;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
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
        return ("Value 1".equals(value) || "Value 2".equals(value)) ? MasterEntitySimpleEntityPropCategory.AVAILABLE : //
                (("Value 3".equals(value)) ? MasterEntitySimpleEntityPropCategory.BROKEN : //
                        (("Value 4".equals(value)) ? MasterEntitySimpleEntityPropCategory.UNOPERATIONAL : //
                                MasterEntitySimpleEntityPropCategory.UNCATEGORIZED));
    }

    @Override
    public List<? extends ICategory> getAllCategories() {
        return Arrays.asList(MasterEntitySimpleEntityPropCategory.AVAILABLE, MasterEntitySimpleEntityPropCategory.BROKEN, MasterEntitySimpleEntityPropCategory.UNOPERATIONAL);
    }

    @Override
    public List<? extends ICategory> getMainCategories() {
        return Arrays.asList(MasterEntitySimpleEntityPropCategory.AVAILABLE, MasterEntitySimpleEntityPropCategory.BROKEN);
    }

    /**
     * Categories demo/test implementation.
     * 
     * @author Tg Team
     * 
     */
    public static enum MasterEntitySimpleEntityPropCategory implements ICategory {
        AVAILABLE("Available", "Smth is available (Value 1, Value 2)", Color.BLUE), //
        BROKEN("Broken", "Smth is broken (Value 3)", Color.YELLOW), //
        UNOPERATIONAL("Unoperational", "Smth is completely un-operational(Value 4)", Color.RED), //
        UNCATEGORIZED("Non-categorized", "Non-categorized", null);

        private final String title;
        private final String desc;
        private final Color color;

        private MasterEntitySimpleEntityPropCategory(final String title, final String desc, final Color color) {
            this.title = title;
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