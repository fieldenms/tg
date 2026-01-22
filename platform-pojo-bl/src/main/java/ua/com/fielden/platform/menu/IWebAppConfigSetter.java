package ua.com.fielden.platform.menu;

public interface IWebAppConfigSetter {

    IWebAppConfigSetter setMinDesktopWidth(final int width);

    IWebAppConfigSetter setMinTabletWidth(final int width);

    IWebAppConfigSetter setLocale(final String locale);

    IWebAppConfigSetter setTimeFormat(final String timeFormat);

    IWebAppConfigSetter setTimeWithMillisFormat(final String timeWithMillisFormat);

    IWebAppConfigSetter setDateFormat(final String dateFormat);
}
