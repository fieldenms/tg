/**
 *
 */
package ua.com.fielden.platform.swing.egi.models;

import java.awt.Component;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IHierarchyProvider;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.models.builders.HierarchicalPropertyTableModelBuilder;

import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableComponentFactory;
import com.jidesoft.grid.HierarchicalTableModel;

/**
 * Hierarchical table model for {@link EntityGridInspector} in case when properties are read-only.
 * 
 * @author Yura
 * 
 * @param <ParentType>
 */
@SuppressWarnings("unchecked")
public class HierarchicalPropertyTableModel<ParentType extends AbstractEntity> extends PropertyTableModel<ParentType> {

    private static final long serialVersionUID = 1L;

    private final IHierarchyProvider<ParentType> provider;
    private boolean hierarchical = true;
    private boolean alwaysExpandable = false;
    private final HierarchicalTableComponentFactory componentFactory;

    /**
     * Uses passed builder instance and its mappings, provider and component factory
     * 
     * @param builder
     * @param instances
     */
    public HierarchicalPropertyTableModel(final HierarchicalPropertyTableModelBuilder<ParentType> builder, final List<ParentType> instances) {
        super(builder, instances);

        this.provider = builder.getHierarchyProvider();
        this.componentFactory = builder.getComponentFactory();
    }

    @Override
    public Object getChildValueAt(final int row) {
        return instance(row);
    }

    @Override
    public boolean hasChild(final int row) {
        return isDataRow(row) && (isAlwaysExpandable() || provider.hasChildren(instance(row)));
    }

    @Override
    public boolean isExpandable(final int row) {
        return isDataRow(row) && (isAlwaysExpandable() || hasChild(row));
    }

    /**
     * This implementation disregards the <code>row</code> parameter returning the same result for all rows.
     */
    @Override
    public boolean isHierarchical(final int row) {
        return isDataRow(row) && isHierarchical();
    }

    public boolean isHierarchical() {
        return hierarchical;
    }

    /**
     * If set to <code>true</code> methods {@link HierarchicalTableModel#isHierarchical(int)} always return <code>true</code>.
     * 
     * @param hierarchical
     */
    public void setHierarchical(final boolean hierarchical) {
        this.hierarchical = hierarchical;
    }

    public boolean isAlwaysExpandable() {
        return alwaysExpandable;
    }

    /**
     * If set to <code>true</code> methods {@link HierarchicalTableModel#isExpandable(int)} and {@link HierarchicalTableModel#hasChild(int)} always return <code>true</code>.
     * <p>
     * This basically enforces expandability of every row in the grid regardless whether it has children or not.
     * 
     * @param alwaysExpandable
     */
    public void setAlwaysExpandable(final boolean alwaysExpandable) {
        this.alwaysExpandable = alwaysExpandable;
    }

    @Override
    public Component createChildComponent(final HierarchicalTable table, final Object value, final int row) {
        return componentFactory.createChildComponent(table, value, row);
    }

    @Override
    public void destroyChildComponent(final HierarchicalTable table, final Component component, final int row) {
        componentFactory.destroyChildComponent(table, component, row);
    }

}
