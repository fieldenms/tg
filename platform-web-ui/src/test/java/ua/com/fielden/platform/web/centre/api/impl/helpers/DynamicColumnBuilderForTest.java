package ua.com.fielden.platform.web.centre.api.impl.helpers;

import java.util.Optional;

import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;
import ua.com.fielden.platform.web.centre.api.resultset.IDynamicColumnBuilder;

/// Stub implementation of [IDynamicColumnBuilder] for testing.
/// Returns no dynamic columns so the DSL data model can be inspected without involving rendering.
///
public class DynamicColumnBuilderForTest implements IDynamicColumnBuilder<TgVehicle> {

    @Override
    public Optional<IDynamicColumnConfig> getColumnsConfig(final Optional<CentreContext<TgVehicle, ?>> context) {
        return Optional.empty();
    }

}
