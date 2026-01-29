package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.menu.IWebAppConfigProvider;
import ua.com.fielden.platform.tiny.TinyHyperlink;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

public class ApplicationConfigurationResource extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ERR_DEVICE_SCREEN_WIDTH = "The desktop width can not be less then or equal tablet width.";

    private final RestServerUtil restUtil;

    private final IWebAppConfigProvider webAppConfigProvider;
    private final IApplicationSettings appSettings;
    private final IDates dates;

    public ApplicationConfigurationResource (
            final RestServerUtil restUtil,

            final IWebAppConfigProvider webAppConfigProvider,
            final IApplicationSettings appSettings,
            final IDeviceProvider deviceProvider,
            final IDates dates,

            final Context context,
            final Request request,
            final Response response)
    {
        super(context, request, response, deviceProvider, dates);

        this.restUtil = restUtil;
        this.webAppConfigProvider = webAppConfigProvider;
        this.appSettings = appSettings;
        this.dates = dates;
    }

    /// Handles a GET request to open a [TinyHyperlink].
    ///
    @Get
    public Representation get() {
        if (webAppConfigProvider.minDesktopWidth() <= webAppConfigProvider.minTabletWidth()) {
            LOGGER.error(ERR_DEVICE_SCREEN_WIDTH);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return restUtil.webApiResultRepresentation(CollectionUtil.linkedMapOf(T2.t2("errorMsg", ERR_DEVICE_SCREEN_WIDTH)));
        }
        final Map<String, Object> configs = new LinkedHashMap<>();
        configs.put("siteAllowlist", appSettings.siteAllowList());
        configs.put("daysUntilSitePermissionExpires", appSettings.daysUntilSitePermissionExpires());
        configs.put("currencySymbol", appSettings.currencySymbol());
        configs.put("timeZone", webAppConfigProvider.independentTimeZone() ? TimeZone.getDefault().getID() : "");
        configs.put("minDesktopWidth", webAppConfigProvider.minDesktopWidth());
        configs.put("minTabletWidth", webAppConfigProvider.minTabletWidth());
        configs.put("locale", webAppConfigProvider.locale());
        configs.put("dateFormat", webAppConfigProvider.dateFormat());
        configs.put("timeFormat", webAppConfigProvider.timeFormat());
        configs.put("masterActionOptions", webAppConfigProvider.masterActionOptions());
        configs.put("timeWithMillisFormat", webAppConfigProvider.timeWithMillisFormat());
        // Need to set the first day of week, which is used by the date picker component to correctly render a weekly representation of a month.
        // Because IDates use a number range from 1 to 7 to represent Mon to Sun and JS uses 0 for Sun, we need to convert the value coming from IDates.
        configs.put("firstDayOfWeek", dates.startOfWeek() % 7);
        configs.put("title", webAppConfigProvider.title());
        configs.put("ideaUri", webAppConfigProvider.ideaUri());
        configs.put("panelColor", webAppConfigProvider.mainPanelColor());
        configs.put("watermark", webAppConfigProvider.watermark());
        configs.put("watermarkStyle", webAppConfigProvider.watermarkStyle());
        return restUtil.webApiResultRepresentation(configs);
    }
}
