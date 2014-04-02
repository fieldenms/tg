package ua.com.fielden.platform.swing.egi;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IHierarchyProvider;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.HierarchicalPropertyTableModelBuilder;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableComponentFactory;

public abstract class HierarchicalEgiPanel<T extends AbstractEntity<?>> extends EgiPanel<T> {

    private static final long serialVersionUID = 6268427298952957353L;

    private final Map<Object, Component> subComponents = new HashMap<>();

    public HierarchicalEgiPanel(final Class<T> rootType, final ICentreDomainTreeManagerAndEnhancer cdtme) {
        this(rootType, cdtme, null);
    }

    public HierarchicalEgiPanel(final Class<T> rootType, final ICentreDomainTreeManagerAndEnhancer cdtme, final IColouringScheme<T> egiColouringScheme) {
        super(rootType, cdtme, egiColouringScheme);
    }

    protected abstract SubEgiPanel<T, ?> createSubGridPanel(final HierarchicalTable table, final Object value, final int row);

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected PropertyTableModel<?> createGridModelWithColouringScheme(final Class<?> managedType, final List<Pair<String, Integer>> gridDataModel, final IColouringScheme egiColouringScheme) {
        return createTableModelBuilder(managedType, gridDataModel).//
        setRowColoringScheme(egiColouringScheme).//
        build(new ArrayList());
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected PropertyTableModel<?> createGridModel(final Class<?> managedType, final List<Pair<String, Integer>> gridDataModel) {
        return createTableModelBuilder(managedType, gridDataModel).build(new ArrayList());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private HierarchicalPropertyTableModelBuilder<?> createTableModelBuilder(final Class<?> managedType, final List<Pair<String, Integer>> gridDataModel) {
        final HierarchicalPropertyTableModelBuilder<?> tableModelBuilder = new HierarchicalPropertyTableModelBuilder(managedType, new IHierarchyProvider() {

            @Override
            public boolean hasChildren(final Object parentEntity) {
                return true;
            }
        }, createSubEgiFacotry());
        for (final Pair<String, Integer> property : gridDataModel) {
            tableModelBuilder.addReadonly(property.getKey(), property.getValue());
        }
        return tableModelBuilder;
    }

    private HierarchicalTableComponentFactory createSubEgiFacotry() {
        return new HierarchicalTableComponentFactory() {

            @Override
            public void destroyChildComponent(final HierarchicalTable table, final Component value, final int row) {
            }

            @Override
            public Component createChildComponent(final HierarchicalTable table, final Object value, final int row) {
                Component subComponent = subComponents.get(value);
                if (subComponent == null) {
                    subComponent = createSubGridPanel(table, value, row);
                    subComponents.put(value, subComponent);
                }
                return subComponent;
            }
        };
    }

    @Override
    public void setData(final IPage<T> page) {
        super.setData(page);
        subComponents.clear();
    }
}
