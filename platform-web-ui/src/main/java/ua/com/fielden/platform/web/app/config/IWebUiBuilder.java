package ua.com.fielden.platform.web.app.config;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import java.util.Optional;
import java.util.function.Supplier;

/// A contract for building a Web UI configuration.
///
public interface IWebUiBuilder {

    /// Specifies the main environmental top panel style and its watermark.
    ///
    /// @param backgroundColour a background HTML colour for the top panel, e.g. #44750C.
    /// @param watermark        a text to be as a custom label in the middle of the top panel.
    /// @param cssWatermark     CSS to style the watermark text (e.g. `font-weight:bold;opacity:0.5`).
    ///
    IWebUiBuilder withTopPanelStyle(final Optional<String> backgroundColour, final Optional<String> watermark, final Optional<String> cssWatermark);

    /// Sets the minimal desktop width.
    ///
    IWebUiBuilder setMinDesktopWidth(int width);

    /// Sets the minimal tablet width
    ///
    IWebUiBuilder setMinTabletWidth(int width);

    /// Sets the locale.
    ///
    IWebUiBuilder setLocale(String locale);

    /// Sets the date format.
    ///
    IWebUiBuilder setDateFormat(String dateFormat);

    /// Sets the time format.
    ///
    IWebUiBuilder setTimeFormat(String timeFormat);

    /// Sets the time with millis format.
    ///
    IWebUiBuilder setTimeWithMillisFormat(String timeWithMillisFormat);

    /// Registers `master` with this configuration.
    ///
    /// @throws WebUiBuilderException If a master for the entity type associated with `master` has already been registered.
    ///
    /// @return This builder.
    ///
    <T extends AbstractEntity<?>> IWebUiBuilder addMaster(final EntityMaster<T> master);

    /// Registers `master` with this configuration.
    ///
    /// @throws WebUiBuilderException If a master for the entity type associated with `master` has already been registered.
    ///
    /// @return The `master` object.
    ///
    <ENTITY_TYPE extends AbstractEntity<?>> EntityMaster<ENTITY_TYPE> register(final EntityMaster<ENTITY_TYPE> master);

    /// Returns an optional describing a master for the specified entity type.
    /// The value is present iff the master is registered.
    ///
    <T extends AbstractEntity<?>> Optional<EntityMaster<T>> getMaster(final Class<T> entityType);

    /// Registers `centre` with this configuration.
    ///
    /// @throws WebUiBuilderException If a centre for the entity type associated with `centre` has already been registered.
    ///
    /// @return The `centre` object.
    ///
    <ENTITY_TYPE extends AbstractEntity<?>> EntityCentre<ENTITY_TYPE> register(final EntityCentre<ENTITY_TYPE> centre);

    /// Registers `centre` with this configuration.
    ///
    /// @throws WebUiBuilderException If a centre for the entity type associated with `centre` has already been registered.
    ///
    /// @return This builder.
    ///
    <M extends MiWithConfigurationSupport<?>> IWebUiBuilder addCentre(final EntityCentre<?> centre);

    /// Returns an optional describing a centre for the specified entity type.
    /// The value is present iff the centre is registered.
    ///
    <M extends MiWithConfigurationSupport<?>> Optional<EntityCentre<?>> getCentre(final Class<M> menuType);

    /// Registers a custom view with this configuration.
    ///
    IWebUiBuilder addCustomView(final AbstractCustomView customView);

    /// Builds the configuration.
    ///
    IWebUiConfig done();

    /// Registers (associates and caches) entity action configuration `openMasterActionConfig` for `entityType`.
    /// `openMasterActionConfig` must represent an action to open an Entity Master for `entityType`.
    ///
    /// @throws WebUiBuilderException If an action configuration is registered with the same entity type more than once.
    ///
    <T extends AbstractEntity<?>> IWebUiBuilder registerOpenMasterAction(final Class<T> entityType, final EntityActionConfig openMasterActionConfig);

    /// Returns a supplier to lazily obtain an "open entity master" action configuration for `entityType`.
    /// The returned supplier is never `null`, but its result is optional and could be empty.
    ///
    <T extends AbstractEntity<?>> Supplier<Optional<EntityActionConfig>> getOpenMasterAction(final Class<T> entityType);

    /// Register `actionConfig` as an "extra" action that is not exposed in the UI, but exists for other server-side purposes.
    ///
    IWebUiBuilder registerExtraAction(EntityActionConfig actionConfig);

}
