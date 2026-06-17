package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.crit.layout.ILayoutConfigWithResultsetSupport;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder1aEgiAppearance;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.AbstractLayout;
import ua.com.fielden.platform.web.layout.ILayoutConfiguration;

/// A package private helper class to decompose the task of implementing the Entity Centre DSL.
/// It has direct access to protected fields in [EntityCentreBuilder].
///
class SelectionCriteriaLayoutBuilder<T extends AbstractEntity<?>> extends ResultSetBuilder<T> implements ILayoutConfigWithResultsetSupport<T> {

    public SelectionCriteriaLayoutBuilder(final EntityCentreBuilder<T> builder) {
        super(builder);
    }

    @Override
    public ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final ILayoutConfiguration config) {
        if (device == null || orientation == null) {
            throw new IllegalArgumentException("Selection criterial layout requries device and orientation (optional) to be specified.");
        }
        // The first configuration determines the layout kind: replace the default flex manager with the configured kind's manager.
        // A single client element renders all breakpoints, so every subsequent breakpoint must resolve to the same kind of manager.
        final AbstractLayout<?> manager = config.mkLayoutManager("sel_crit");
        if (!this.builder.selectionCriteriaLayoutConfigured) {
            this.builder.selectionCriteriaLayout = manager;
            this.builder.selectionCriteriaLayoutConfigured = true;
        } else if (!this.builder.selectionCriteriaLayout.getClass().equals(manager.getClass())) {
            throw new IllegalArgumentException("All selection-criteria layouts for a centre must be of the same kind. Cannot mix [%s] with [%s].".formatted(this.builder.selectionCriteriaLayout.getClass().getSimpleName(), manager.getClass().getSimpleName()));
        }
        this.builder.selectionCriteriaLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(config.layout());
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
