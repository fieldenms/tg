package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.crit.layout.IGridLayoutConfigWithResultsetSupport;
import ua.com.fielden.platform.web.centre.api.crit.layout.ILayoutConfigWithResultsetSupport;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1aEgiAppearance;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.GridLayout;
import ua.com.fielden.platform.web.layout.grid.IGridLayoutConfiguration;

/// A package private helper class to decompose the task of implementing the Entity Centre DSL.
/// It has direct access to protected fields in [EntityCentreBuilder].
///
class SelectionCriteriaLayoutBuilder<T extends AbstractEntity<?>> extends ResultSetBuilder<T> implements ILayoutConfigWithResultsetSupport<T>, IGridLayoutConfigWithResultsetSupport<T> {

    public SelectionCriteriaLayoutBuilder(final EntityCentreBuilder<T> builder) {
        super(builder);
    }

    @Override
    public ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalArgumentException("Selection criterial layout requries device and orientation (optional) to be specified.");
        }
        // The selection-criteria layout is flex; the default manager is already a FlexLayout.
        this.builder.selectionCriteriaLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }

    @Override
    public IGridLayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final IGridLayoutConfiguration grid) {
        if (device == null || orientation == null) {
            throw new IllegalArgumentException("Selection criterial layout requries device and orientation (optional) to be specified.");
        }
        // The selection-criteria layout is grid; replace the default flex manager with a GridLayout on the first breakpoint.
        if (!(this.builder.selectionCriteriaLayout instanceof GridLayout)) {
            this.builder.selectionCriteriaLayout = new GridLayout("sel_crit");
        }
        this.builder.selectionCriteriaLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(grid.layout());
        return this;
    }

    @Override
    public <G extends AbstractEntity<?> & WithCreatedByUser<G>> IResultSetBuilder1aEgiAppearance<T> withGenerator(final Class<G> entityTypeToBeGenerated, final Class<? extends IGenerator<G>> generator) {
        if (entityTypeToBeGenerated == null || generator == null) {
            throw new IllegalArgumentException("Generator definition requries both types to be specified (generator type and entityTypeToBeGenerated).");
        }
        this.builder.generatorTypes = Pair.pair(entityTypeToBeGenerated, generator);
        return this;
    }

}
