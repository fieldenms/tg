package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.menu.IWebAppConfigProvider;
import ua.com.fielden.platform.utils.IDates;

import java.util.TimeZone;

import static org.apache.logging.log4j.LogManager.getLogger;

public class ApplicationConfigEntityProducer extends DefaultEntityProducerWithContext<ApplicationConfigEntity> {

    private static final Logger LOGGER = getLogger(ApplicationConfigEntityProducer.class);
    public static final String ERR_DEVICE_SCREEN_WIDTH = "The desktop width can not be less then or equal tablet width.";

    private final IWebAppConfigProvider webAppConfigProvider;
    private final IApplicationSettings appSettings;
    private final IDates dates;

    @Inject
    public ApplicationConfigEntityProducer(
            final IWebAppConfigProvider webAppConfigProvider,
            final ICompanionObjectFinder coFinder,
            final EntityFactory entityFactory,
            final IApplicationSettings appSettings,
            final IDates dates) {
        super(entityFactory, ApplicationConfigEntity.class, coFinder);
        this.webAppConfigProvider = webAppConfigProvider;
        this.appSettings = appSettings;
        this.dates = dates;
    }

    @Override
    protected ApplicationConfigEntity provideDefaultValues(final ApplicationConfigEntity appConfig) {
        if (webAppConfigProvider.minDesktopWidth() <= webAppConfigProvider.minTabletWidth()) {
            throw new IllegalStateException(ERR_DEVICE_SCREEN_WIDTH);
        }
        appConfig.setSiteAllowlist(appSettings.siteAllowList());
        appConfig.setDaysUntilSitePermissionExpires(appSettings.daysUntilSitePermissionExpires());
        appConfig.setCurrencySymbol(appSettings.currencySymbol());
        appConfig.setTimeZone(webAppConfigProvider.independentTimeZone() ? TimeZone.getDefault().getID() : "");
        appConfig.setMinDesktopWidth(webAppConfigProvider.minDesktopWidth());
        appConfig.setMinTabletWidth(webAppConfigProvider.minTabletWidth());
        appConfig.setLocale(webAppConfigProvider.locale());
        appConfig.setDateFormat(webAppConfigProvider.dateFormat());
        appConfig.setTimeFormat(webAppConfigProvider.timeFormat());
        appConfig.setMasterActionOptions(webAppConfigProvider.masterActionOptions());
        appConfig.setTimeWithMillisFormat(webAppConfigProvider.timeWithMillisFormat());
        // Need to set the first day of week, which is used by the date picker component to correctly render a weekly representation of a month.
        // Because IDates use a number range from 1 to 7 to represent Mon to Sun and JS uses 0 for Sun, we need to convert the value coming from IDates.
        appConfig.setFirstDayOfWeek(dates.startOfWeek() % 7);
        return super.provideDefaultValues(appConfig);
    }
}
