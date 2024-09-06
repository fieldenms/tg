package ua.com.fielden.platform.ioc;

import com.google.inject.AbstractModule;

import ua.com.fielden.platform.persistence.types.ColourType;
import ua.com.fielden.platform.persistence.types.HyperlinkType;
import ua.com.fielden.platform.persistence.types.MoneyType;
import ua.com.fielden.platform.persistence.types.MoneyWithTaxAmountType;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SecurityTokenType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.persistence.types.SimplyMoneyWithTaxAmountType;
import ua.com.fielden.platform.persistence.types.SimplyMoneyWithTaxAndExTaxAmountType;
import ua.com.fielden.platform.persistence.types.UtcDateTimeType;
import ua.com.fielden.platform.types.markers.IColourType;
import ua.com.fielden.platform.types.markers.IHyperlinkType;
import ua.com.fielden.platform.types.markers.IMoneyType;
import ua.com.fielden.platform.types.markers.IMoneyWithTaxAmountType;
import ua.com.fielden.platform.types.markers.IPropertyDescriptorType;
import ua.com.fielden.platform.types.markers.ISecurityTokenType;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;
import ua.com.fielden.platform.types.markers.ISimplyMoneyWithTaxAmountType;
import ua.com.fielden.platform.types.markers.ISimplyMoneyWithTaxAndExTaxAmountType;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

/**
 * Guice injector module for binding between custom hibernate types and their interfaces-markers.
 * 
 * @author TG Team
 * 
 */
public class HibernateUserTypesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ISecurityTokenType.class).to(SecurityTokenType.class);
        bind(IPropertyDescriptorType.class).to(PropertyDescriptorType.class);
        bind(IColourType.class).to(ColourType.class);
        bind(IHyperlinkType.class).to(HyperlinkType.class);
        bind(IUtcDateTimeType.class).to(UtcDateTimeType.class);

        bind(IMoneyType.class).to(MoneyType.class);
        bind(ISimpleMoneyType.class).to(SimpleMoneyType.class);
        bind(ISimplyMoneyWithTaxAndExTaxAmountType.class).to(SimplyMoneyWithTaxAndExTaxAmountType.class);
        bind(ISimplyMoneyWithTaxAmountType.class).to(SimplyMoneyWithTaxAmountType.class);
        bind(IMoneyWithTaxAmountType.class).to(MoneyWithTaxAmountType.class);
    }
}
