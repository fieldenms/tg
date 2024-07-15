package ua.com.fielden.platform.web.test.server;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.persistence.types.ColourType;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.types.HyperlinkType;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

/**
 * Used for setting up Hibernate specific type resolutions in Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class HibernateSetup {

    private static final Map<Class, Class> hibTypeDefaults = new HashMap<>();

    static {
        hibTypeDefaults.put(boolean.class, YesNoType.class);
        hibTypeDefaults.put(Boolean.class, YesNoType.class);
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(PropertyDescriptor.class, PropertyDescriptorType.class);
        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
        hibTypeDefaults.put(Colour.class, ColourType.class);
        hibTypeDefaults.put(Hyperlink.class, HyperlinkType.class);
    }

    public static Map<Class, Class> getHibernateTypes() {
        return Collections.unmodifiableMap(hibTypeDefaults);
    }

}
