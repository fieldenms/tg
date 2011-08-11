package ua.com.fielden.platform.treemodel;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;

/**
 * Tree model for single entity. Could contain other "entities" marked as "collectional" properties for main entity.
 *
 * @author TG Team
 *
 */
public class EntityTreeModel extends EntitiesTreeModel {
    private static final long serialVersionUID = 1L;

    private final Class<? extends AbstractEntity> mainClass;
    private final List<Class<? extends AbstractEntity>> collectionalPropertyTypes = new ArrayList<Class<? extends AbstractEntity>>();

    /**
     * Creates tree model for single entity. Could contain other "entities" marked as "collectional" properties for main entity.
     *
     * @param mainClass - the Main entity class for this single-entity tree model.
     */
    public EntityTreeModel(final Class<? extends AbstractEntity> mainClass, final IPropertyFilter propertyFilter, final boolean ignoreCollections) {
	super(new ArrayList<Class<? extends AbstractEntity>>(){{add(mainClass);}}, propertyFilter);
	this.mainClass = mainClass;

	if (!ignoreCollections){
	    // add "collectional" properties related to "mainClass"
	    this.collectionalPropertyTypes.addAll(AnnotationReflector.getCollectionalPropertyTypes(this.mainClass));
	    if (!collectionalPropertyTypes.isEmpty()) {
		addEntities(collectionalPropertyTypes, mainClass);
	    }
	}
    }

    /** Returns true if specified class is collectional property type for Main class.*/
    public boolean isCollectionalPropertyType(final Class<?> type){
	return collectionalPropertyTypes.contains(type);
    }

    /** Returns a list of collectional property types for main entity. */
    protected List<Class<? extends AbstractEntity>> getCollectionalPropertyTypes() {
        return collectionalPropertyTypes;
    }
}
