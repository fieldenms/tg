package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;

import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.crit.layout.ILayoutConfigWithResultsetSupport;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder0Checkbox;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaLayoutBuilder<T extends AbstractEntity<?>> extends ResultSetBuilder<T> implements ILayoutConfigWithResultsetSupport<T> {

    private final EntityCentreBuilder<T> builder;

    public SelectionCriteriaLayoutBuilder(final EntityCentreBuilder<T> builder) {
        super(builder);
        this.builder = builder;
    }

    @Override
    public ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalArgumentException("Selection criterial layout requries device and orientation (optional) to be specified.");
        }
        this.builder.selectionCriteriaLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }
    
    @Override
    public <G extends AbstractEntity<?> & WithCreatedByUser<G>> IResultSetBuilder0Checkbox<T> withGenerator(final Class<G> entityTypeToBeGenerated, final Class<? extends IGenerator<G>> generator) {
        if (entityTypeToBeGenerated == null || generator == null) {
            throw new IllegalArgumentException("Generator definition requries both types to be specified (generator type and entityTypeToBeGenerated).");
        }
        this.builder.generatorTypes = Pair.pair(entityTypeToBeGenerated, generator);
        return this;
    }

}
