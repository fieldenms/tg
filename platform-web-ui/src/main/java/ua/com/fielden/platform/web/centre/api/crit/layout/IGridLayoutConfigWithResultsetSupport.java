package ua.com.fielden.platform.web.centre.api.crit.layout;

import java.util.Optional;

import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1aEgiAppearance;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.grid.IGridLayoutConfiguration;

/// The grid selection-criteria layout continuation: further grid breakpoints, the generator option, and the result-set builder.
/// It does not expose the flex `setLayoutFor`, so selection criteria that started grid stay grid.
///
public interface IGridLayoutConfigWithResultsetSupport<T extends AbstractEntity<?>> extends IResultSetBuilder1aEgiAppearance<T> {

    /// Installs a grid selection-criteria layout for a further `device` and `orientation`.
    ///
    IGridLayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final IGridLayoutConfiguration grid);

    /// Augments the centre's selection criteria definition with a data [IGenerator].
    ///
    /// @param entityTypeToBeGenerated -- the type of entities to be generated; must implement [WithCreatedByUser] to support automatic filtering of generated data
    /// @param generatorType -- the type of generator
    ///
    <G extends AbstractEntity<?> & WithCreatedByUser<G>> IResultSetBuilder1aEgiAppearance<T> withGenerator(final Class<G> entityTypeToBeGenerated, final Class<? extends IGenerator<G>> generatorType);
}