package ua.com.fielden.platform.persistence.types;

import org.hibernate.type.YesNoType;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.*;
import ua.com.fielden.platform.types.markers.*;

import java.util.Date;

public final class PlatformHibernateTypeMappings {

    public static final HibernateTypeMappings PLATFORM_HIBERNATE_TYPE_MAPPINGS = HibernateTypeMappings.builder()
            .put(boolean.class, YesNoType.INSTANCE)
            .put(Boolean.class, YesNoType.INSTANCE)
            .put(Date.class, DateTimeType.INSTANCE)
            .put(PropertyDescriptor.class, PropertyDescriptorType.INSTANCE)
            .put(Money.class, SimpleMoneyType.INSTANCE)
            .put(Colour.class, ColourType.INSTANCE)
            .put(Hyperlink.class, HyperlinkType.INSTANCE)
            .put(RichText.class, RichTextType.INSTANCE)

            .put(ISecurityTokenType.class, SecurityTokenType.INSTANCE)
            .put(IPropertyDescriptorType.class, PropertyDescriptorType.INSTANCE)
            .put(IColourType.class, ColourType.INSTANCE)
            .put(IHyperlinkType.class, HyperlinkType.INSTANCE)
            .put(IUtcDateTimeType.class, UtcDateTimeType.INSTANCE)
            .put(IRichTextType.class, RichTextType.INSTANCE)

            .put(IMoneyUserType.class, MoneyUserType.INSTANCE)
            .put(ISimpleMoneyType.class, SimpleMoneyType.INSTANCE)
            .put(ISimplyMoneyWithTaxAndExTaxAmountType.class, SimplyMoneyWithTaxAndExTaxAmountType.INSTANCE)
            .put(ISimplyMoneyWithTaxAmountType.class, SimplyMoneyWithTaxAmountType.INSTANCE)
            .put(IMoneyWithTaxAmountUserType.class, MoneyWithTaxAmountUserType.INSTANCE)

            .build();

    private PlatformHibernateTypeMappings() {}

}
