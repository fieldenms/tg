package ua.com.fielden.platform.persistence.types;

import com.google.inject.Inject;
import org.hibernate.type.YesNoType;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.types.*;
import ua.com.fielden.platform.types.markers.*;

import java.util.Date;

/**
 * Mappings of primitive and component types to Hibernate user types.
 * <p>
 * {@link Provider} should be bound in a Guice module: {@code bind(HibernateTypeMappings.class).toProvider(PlatformHibernateTypeMappings.Provider.class)},
 * which will enable access to a {@link HibernateTypeMappings} instance via an injector.
 * <p>
 * This information used to be provided by {@code HibernateUserTypesModule} and {@code HibernateSetup} at the level of applications.
 */
public final class PlatformHibernateTypeMappings {

    public static final class Provider implements jakarta.inject.Provider<HibernateTypeMappings> {

        private final IDbVersionProvider dbVersionProvider;

        @Inject
        public Provider(final IDbVersionProvider dbVersionProvider) {
            this.dbVersionProvider = dbVersionProvider;
        }

        @Override
        public HibernateTypeMappings get() {
            final var richTextType = RichTextType.getInstance(dbVersionProvider.dbVersion());

            return HibernateTypeMappings.builder()
                    .put(boolean.class, YesNoType.INSTANCE)
                    .put(Date.class, DateTimeType.INSTANCE)
                    .put(PropertyDescriptor.class, PropertyDescriptorType.INSTANCE)
                    .put(Money.class, SimpleMoneyType.INSTANCE)
                    .put(Colour.class, ColourType.INSTANCE)
                    .put(Hyperlink.class, HyperlinkType.INSTANCE)
                    .put(RichText.class, richTextType)

                    .put(ISecurityTokenType.class, SecurityTokenType.INSTANCE)
                    .put(IPropertyDescriptorType.class, PropertyDescriptorType.INSTANCE)
                    .put(IColourType.class, ColourType.INSTANCE)
                    .put(IHyperlinkType.class, HyperlinkType.INSTANCE)
                    .put(IUtcDateTimeType.class, UtcDateTimeType.INSTANCE)
                    .put(IRichTextType.class, richTextType)

                    .put(IMoneyType.class, MoneyType.INSTANCE)
                    .put(ISimpleMoneyType.class, SimpleMoneyType.INSTANCE)
                    .put(ISimplyMoneyWithTaxAndExTaxAmountType.class, SimplyMoneyWithTaxAndExTaxAmountType.INSTANCE)
                    .put(ISimplyMoneyWithTaxAmountType.class, SimplyMoneyWithTaxAmountType.INSTANCE)
                    .put(IMoneyWithTaxAmountType.class, MoneyWithTaxAmountType.INSTANCE)
                    .build();
        }
    }

}
