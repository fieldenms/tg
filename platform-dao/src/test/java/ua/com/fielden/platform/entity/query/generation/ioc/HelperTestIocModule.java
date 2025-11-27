package ua.com.fielden.platform.entity.query.generation.ioc;

import com.google.inject.name.Names;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.utils.DefaultUniversalConstants;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

/// This is a helper IoC module that should be used (and enhanced if needed) with dependency bindings of named values for testing purposes that do not belong elsewhere.
///
public class HelperTestIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("app.name")).to("Test");
        bindConstant().annotatedWith(Names.named("email.smtp")).to("non-existing-server");
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to("tg@fielden.com.au");
        bindConstant().annotatedWith(Names.named("independent.time.zone")).to(false);
        bindConstant().annotatedWith(Names.named("dates.weekStart")).to(Integer.valueOf(1));
        bindConstant().annotatedWith(Names.named("dates.finYearStartDay")).to(Integer.valueOf(1));
        bindConstant().annotatedWith(Names.named("dates.finYearStartMonth")).to(Integer.valueOf(7));
        bindConstant().annotatedWith(Names.named("dates.dateFormat")).to(IDates.DEFAULT_DATE_FORMAT);
        bindConstant().annotatedWith(Names.named("dates.timeFormat")).to(IDates.DEFAULT_TIME_FORMAT);
        bindConstant().annotatedWith(Names.named("dates.timeFormatWithMillis")).to(IDates.DEFAULT_TIME_FORMAT_WITH_MILLIS);
        bindConstant().annotatedWith(Names.named("dates.dateFormat.web")).to(IDates.DEFAULT_DATE_FORMAT_WEB);
        bindConstant().annotatedWith(Names.named("dates.timeFormat.web")).to(IDates.DEFAULT_TIME_FORMAT_WEB);
        bindConstant().annotatedWith(Names.named("dates.timeFormatWithMillis.web")).to(IDates.DEFAULT_TIME_FORMAT_WEB_WITH_MILLIS);

        bind(IDates.class).to(DefaultDates.class);
        bind(IUniversalConstants.class).to(DefaultUniversalConstants.class);
    }

}
