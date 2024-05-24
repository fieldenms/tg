package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Set;

final class TypeRegistry {

    public static final Set<Class<?>> COMPOSITE_TYPES = Set.of(Money.class);

    public static final Set<Class<?>> PRIMITIVE_PROPERTY_TYPES = Set.of(
            String.class, Long.class, Integer.class, BigDecimal.class, Date.class, boolean.class, Boolean.class, byte[].class,
            Colour.class, Hyperlink.class, Currency.class);

    public static final Set<Class<?>> VALUE_ENTITY_TYPES = Set.of(PropertyDescriptor.class);

    private TypeRegistry() {}

}
