/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.egi.models.GroupingAlgorithm;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;

/**
 * General superclass for all {@link EntityGridInspector} model builders. Contains entity {@link Class} instance along with {@link AbstractPropertyColumnMapping}s for columns
 * 
 * @author Yura
 * 
 * @param <EntityType>
 * @param <TableModelType>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractTableModelBuilder<EntityType extends AbstractEntity, TableModelType extends PropertyTableModel<EntityType>, B extends AbstractTableModelBuilder<EntityType, TableModelType, B>> {

    private final List<AbstractPropertyColumnMapping<EntityType>> propertyColumnMappings = new ArrayList<AbstractPropertyColumnMapping<EntityType>>();

    private GroupingAlgorithm<EntityType> groupingAlgo;

    private final Class<EntityType> entityClass;

    private IColouringScheme<EntityType> rowColoringScheme;

    private final Map<String, IColouringScheme<EntityType>> propertyColoringSchemes = new HashMap<String, IColouringScheme<EntityType>>();

    /**
     * Creates instance of this class and sets entity class property
     * 
     * @param enclosingClass
     */
    public AbstractTableModelBuilder(final Class<EntityType> enclosingClass) {
	this.entityClass = enclosingClass;
    }

    /**
     * Adds passed {@link AbstractPropertyColumnMapping} to the end of mappings list.
     * 
     * @param propertyColumnMapping
     */
    public B add(final AbstractPropertyColumnMapping<EntityType> propertyColumnMapping) {
	if (propertyColumnMapping == null) {
	    throw new NullPointerException();
	}
	propertyColumnMappings.add(propertyColumnMapping);
	return (B) this;
    }

    public B setGroupingAlgo(final GroupingAlgorithm<EntityType> groupingAlgo) {
	this.groupingAlgo = groupingAlgo;
	return (B) this;
    }

    public B setRowColoringScheme(final IColouringScheme<EntityType> rowColoringScheme) {
	this.rowColoringScheme = rowColoringScheme;
	return (B) this;
    }

    public B addPropertyColoring(final String propertyName, final IColouringScheme<EntityType> propertyColoringScheme) {
	propertyColoringSchemes.put(propertyName, propertyColoringScheme);
	return (B) this;
    }

    /**
     * @return list of mappings. Change order of these mappings at your own risk.
     */
    public List<AbstractPropertyColumnMapping<EntityType>> getPropertyColumnMappings() {
	return propertyColumnMappings;
    }

    /**
     * @return entity class, passed to constructor
     */
    public Class<EntityType> getEntityClass() {
	return entityClass;
    }

    public GroupingAlgorithm<EntityType> getGroupingAlgo() {
	return groupingAlgo;
    }

    public IColouringScheme<EntityType> getRowColoringScheme() {
	return rowColoringScheme;
    }

    public Map<String, IColouringScheme<EntityType>> getPropertyColoringSchemes() {
	return propertyColoringSchemes;
    }

    /**
     * Should build table model instance with passed list of EntityType instances
     * 
     * @param instances
     * @return
     */
    public abstract TableModelType build(List<EntityType> instances);

}
