package ua.com.fielden.platform.menu;

/// A contract to accumulate client web application configuration (menu, time/date formats etc.)
///
public interface IWebAppConfigProvider {

    /// Returns true if server and client applications operate in the same time-zone, otherwise false.
    /// The only exception is handling of 'now': it calculates based on real user time-zone (and later converts to server time-zone).
    ///
    boolean independentTimeZone();

    /// Returns the minimum screen size at which the device is treated as a desktop.
    ///
    int minDesktopWidth();

    /// Returns the minimum screen size at which the device is treated as a tablet.
    ///
    int minTabletWidth();

    /// Returns the locale that should be used to format numbers on the client side of application.
    ///
    String locale();

    /// Returns the date format to be used on the client side of application.
    ///
    String dateFormat();

    /// Returns the time format to be used on the client side of application.
    ///
    String timeFormat();

    /// Returns the time with milliseconds format to be used on the client side of application.
    ///
    String timeWithMillisFormat();

    /// Returns value that indicates what options should be available in master actions.
    ///
    String masterActionOptions();
}
