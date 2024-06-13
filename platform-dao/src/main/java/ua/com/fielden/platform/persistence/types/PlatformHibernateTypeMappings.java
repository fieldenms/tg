package ua.com.fielden.platform.persistence.types;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.hibernate.type.YesNoType;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Singleton
public class PlatformHibernateTypeMappings implements HibernateTypeMappings {

    private final Map<Class<?>, Object> map;

    @Inject
    public PlatformHibernateTypeMappings() {
        map = ImmutableMap.<Class<?>, Object>builder()
                .put(boolean.class, YesNoType.INSTANCE)
                .put(Boolean.class, YesNoType.INSTANCE)
                .put(Date.class, DateTimeType.INSTANCE)
                .put(PropertyDescriptor.class, PropertyDescriptorType.INSTANCE)
                .put(Money.class, SimpleMoneyType.INSTANCE)
                .put(Colour.class, ColourType.INSTANCE)
                .put(Hyperlink.class, HyperlinkType.INSTANCE)

                .put(ISecurityTokenType.class, SecurityTokenType.INSTANCE)
                .put(IPropertyDescriptorType.class, PropertyDescriptorType.INSTANCE)
                .put(IColourType.class, ColourType.INSTANCE)
                .put(IHyperlinkType.class, HyperlinkType.INSTANCE)
                .put(IUtcDateTimeType.class, UtcDateTimeType.INSTANCE)

                .put(IMoneyUserType.class, MoneyUserType.INSTANCE)
                .put(ISimpleMoneyType.class, SimpleMoneyType.INSTANCE)
                .put(ISimplyMoneyWithTaxAndExTaxAmountType.class, SimplyMoneyWithTaxAndExTaxAmountType.INSTANCE)
                .put(ISimplyMoneyWithTaxAmountType.class, SimplyMoneyWithTaxAmountType.INSTANCE)
                .put(IMoneyWithTaxAmountUserType.class, MoneyWithTaxAmountUserType.INSTANCE)

                .build();
    }

    @Override
    public Optional<Object> getHibernateType(final Class<?> type) {
        return Optional.ofNullable(map.get(type));
    }

}
