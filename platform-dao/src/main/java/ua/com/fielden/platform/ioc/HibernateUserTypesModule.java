package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.persistence.types.ColourType;
import ua.com.fielden.platform.persistence.types.MoneyUserType;
import ua.com.fielden.platform.persistence.types.MoneyWithTaxAmountUserType;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SecurityTokenType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.persistence.types.SimplyMoneyWithTaxAmountType;
import ua.com.fielden.platform.persistence.types.SimplyMoneyWithTaxAndExTaxAmountType;
import ua.com.fielden.platform.types.markers.IColourType;
import ua.com.fielden.platform.types.markers.IMoneyUserType;
import ua.com.fielden.platform.types.markers.IMoneyWithTaxAmountUserType;
import ua.com.fielden.platform.types.markers.IPropertyDescriptorType;
import ua.com.fielden.platform.types.markers.ISecurityTokenType;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;
import ua.com.fielden.platform.types.markers.ISimplyMoneyWithTaxAmountType;
import ua.com.fielden.platform.types.markers.ISimplyMoneyWithTaxAndExTaxAmountType;

import com.google.inject.AbstractModule;

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

        bind(IMoneyUserType.class).to(MoneyUserType.class);
        bind(ISimpleMoneyType.class).to(SimpleMoneyType.class);
        bind(ISimplyMoneyWithTaxAndExTaxAmountType.class).to(SimplyMoneyWithTaxAndExTaxAmountType.class);
        bind(ISimplyMoneyWithTaxAmountType.class).to(SimplyMoneyWithTaxAmountType.class);
        bind(IMoneyWithTaxAmountUserType.class).to(MoneyWithTaxAmountUserType.class);
    }
}
