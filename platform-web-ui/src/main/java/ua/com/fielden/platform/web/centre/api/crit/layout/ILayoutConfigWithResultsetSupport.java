package ua.com.fielden.platform.web.centre.api.crit.layout;

import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.data.generator.WithCreatedByUser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder0Checkbox;

public interface ILayoutConfigWithResultsetSupport<T extends AbstractEntity<?>> extends ILayoutConfig<T>, IResultSetBuilder0Checkbox<T> {
    /**
     * Augments the centre's selection criteria definition with data {@link IGenerator}.
     * 
     * @param entityTypeToBeGenerated -- the type of entities to be generated, requires to implement {@link WithCreatedByUser} contract to support automatic filtering of generated data
     * @param generatorType -- the type of generator
     * @return
     */
    <G extends AbstractEntity<?> & WithCreatedByUser<G>> IResultSetBuilder0Checkbox<T> withGenerator(final Class<G> entityTypeToBeGenerated, final Class<? extends IGenerator<G>> generatorType);
}
