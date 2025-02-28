package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Set;

/**
 * A registry to encode categorisation of types as defined in <a href="https://github.com/fieldenms/tg/wiki/Property-definitions#more-about-types">Types</a>.
 */
final class TypeRegistry {

    public static final Set<Class<?>> COMPONENT_TYPES = Set.of(Money.class, RichText.class);

    public static final Set<Class<?>> PRIMITIVE_PROPERTY_TYPES = Set.of(
            String.class, Long.class, Integer.class, BigDecimal.class, Date.class, boolean.class, byte[].class,
            Colour.class, Hyperlink.class, Currency.class, Class.class);

    public static final Set<Class<?>> VALUE_ENTITY_TYPES = Set.of(AbstractUnionEntity.class, PropertyDescriptor.class);

    private TypeRegistry() {}

}
