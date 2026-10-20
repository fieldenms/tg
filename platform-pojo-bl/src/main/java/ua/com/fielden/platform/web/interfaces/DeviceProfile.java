package ua.com.fielden.platform.web.interfaces;

/// Enumeration of supported web application profiles.
///
/// Each profile can be provided with its own main menu and other settings.
///
/// `DeviceProfile` in `tg-polymer-utils.js` mirrors the names of these constants.
/// The client sends one of them in the `Device-Profile` request header, and `AbstractWebResource` resolves it by name,
/// so renaming a constant here requires renaming it there as well.
///
public enum DeviceProfile {
    /// Desktop device profile for web application.
    ///
    DESKTOP,
    /// Mobile device profile for web application.
    ///
    MOBILE
}
