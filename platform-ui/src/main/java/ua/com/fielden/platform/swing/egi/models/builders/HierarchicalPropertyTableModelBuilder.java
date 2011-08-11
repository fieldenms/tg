package ua.com.fielden.platform.swing.egi.models.builders;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IHierarchyProvider;
import ua.com.fielden.platform.swing.egi.models.HierarchicalPropertyTableModel;

import com.jidesoft.grid.HierarchicalTableComponentFactory;

/**
 * Builder for {@link HierarchicalPropertyTableModel}.
 * 
 * @author Yura
 * 
 * @param <EntityType>
 * @param <ChildType>
 */
@SuppressWarnings("unchecked")
public class HierarchicalPropertyTableModelBuilder<EntityType extends AbstractEntity> extends PropertyTableModelBuilder<EntityType> {

    private final IHierarchyProvider<EntityType> hierarchyProvider;

    private final HierarchicalTableComponentFactory componentFactory;

    /**
     * Constructs instance of this class by setting references to passed parameters, which will be used during creation of {@link HierarchicalPropertyTableModel} instance
     * 
     * @param entityClass
     * @param hierarchyProvider
     * @param componentFactory
     */
    public HierarchicalPropertyTableModelBuilder(final Class<EntityType> entityClass, final IHierarchyProvider<EntityType> hierarchyProvider, final HierarchicalTableComponentFactory componentFactory) {
	super(entityClass);

	this.hierarchyProvider = hierarchyProvider;
	this.componentFactory = componentFactory;
    }

    public IHierarchyProvider<EntityType> getHierarchyProvider() {
	return hierarchyProvider;
    }

    public HierarchicalTableComponentFactory getComponentFactory() {
	return componentFactory;
    }

    /**
     * Creates instance of {@link HierarchicalPropertyTableModel} using passed instances and parameters passed to constructor
     */
    @Override
    public HierarchicalPropertyTableModel<EntityType> build(final List<EntityType> instances) {
	return new HierarchicalPropertyTableModel<EntityType>(this, instances);
    }

}
