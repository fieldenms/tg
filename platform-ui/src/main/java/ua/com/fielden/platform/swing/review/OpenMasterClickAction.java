package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.awt.event.ActionEvent;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;

/**
 * Click action for {@link EntityGridInspector} that attempts to open master frame for the clicked cell if one represents an entity or entity's key.
 * 
 * @author yura
 * 
 */
public class OpenMasterClickAction extends BlockingLayerCommand<AbstractEntity> {

    private static final long serialVersionUID = -641888610365274514L;

    private final String propertyName;

    private final String trimmedPropertyName;

    private final IEntityMasterManager entityMasterFactory;

    private final EntityReview ownerView;

    /**
     * Principle constructor.
     * 
     * @param entityMasterFactory
     * @param propertyName
     * @param ownerView
     * @param provider
     */
    private OpenMasterClickAction(final IEntityMasterManager entityMasterFactory, final String propertyName, final EntityReview ownerView, final IBlockingLayerProvider provider) {
	super("Double-click facility", provider);
	this.propertyName = propertyName;
	this.entityMasterFactory = entityMasterFactory;
	this.ownerView = ownerView;

	// initialising trimmedPropertyName property, by which we can get to entity, we should open master for
	if (isEmpty(propertyName) || KEY.equals(propertyName)) {
	    // this means we should open master for the entity represented by clicked row
	    this.trimmedPropertyName = "";
	} else if (propertyName.endsWith("." + KEY)) {
	    // this means we should simply skip ".key" suffix, and this is how we obtain desired property name
	    this.trimmedPropertyName = propertyName.substring(0, propertyName.length() - 4);
	} else {
	    this.trimmedPropertyName = propertyName;
	}
    }

    /**
     * Convenient constructor.
     * 
     * @param entityMasterFactory
     * @param propertyName
     * @param ownerView
     */
    private OpenMasterClickAction(final IEntityMasterManager entityMasterFactory, final String propertyName, final EntityReview ownerView) {
	this(entityMasterFactory, propertyName, ownerView, new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return ownerView != null ? ownerView.getReviewContract().getBlockingLayer() : null;
	    }

	});
    }

    /**
     * Convenient constructor.
     * 
     * @param entityMasterFactory
     * @param propertyName
     * @param provider
     */
    private OpenMasterClickAction(final IEntityMasterManager entityMasterFactory, final String propertyName, final IBlockingLayerProvider provider) {
	this(entityMasterFactory, propertyName, null, provider);
    }

    @Override
    protected boolean preAction() {
	setMessage("Opening...");
	if (super.preAction()) {
	    return entityMasterFactory != null;
	}
	return false;
    }

    @Override
    protected AbstractEntity action(final ActionEvent e) throws Exception {
	if (!(e.getSource() instanceof AbstractEntity)) {
	    // it will always be not-null instance of AbstractEntity, but just to be sure
	    return null;
	}
	final Object clickedObject = getPropertyValue((AbstractEntity) e.getSource());
	return clickedObject instanceof AbstractEntity ? (AbstractEntity) clickedObject : null;
    }

    @Override
    protected void postAction(final AbstractEntity clickedEntity) {
	super.postAction(clickedEntity);
	if (clickedEntity != null) {
	    entityMasterFactory.<AbstractEntity, IEntityDao<AbstractEntity>> showMaster(clickedEntity, ownerView);
	}
    }

    private Object getPropertyValue(final AbstractEntity entity) {
	return isEmpty(getTrimmedPropertyName()) ? entity : entity.get(getTrimmedPropertyName());
    }

    public String getPropertyName() {
	return propertyName;
    }

    public String getTrimmedPropertyName() {
	return trimmedPropertyName;
    }

    /**
     * This method will enhance {@link AbstractPropertyColumnMapping}s (but only those that show entities, not simple properties) with sole {@link OpenMasterClickAction} instances.
     * 
     * @param mappings
     * @param entityClass
     * @param entityMasterFactory
     * @param ownerView
     */
    public static void enhanceWithClickAction(final Iterable<? extends AbstractPropertyColumnMapping> mappings, final Class entityClass, final IEntityMasterManager entityMasterFactory, final EntityReview ownerView) {
	for (final AbstractPropertyColumnMapping mapping : mappings) {
	    if (propertyIsOfAbstractEntityType(entityClass, mapping.getPropertyName())) {
		mapping.setClickAction(new OpenMasterClickAction(entityMasterFactory, mapping.getPropertyName(), ownerView));
	    }
	}
    }

    public static void enhanceWithBlockingLayer(final Iterable<? extends AbstractPropertyColumnMapping> mappings, final Class entityClass, final IEntityMasterManager entityMasterFactory, final IBlockingLayerProvider provider) {
	for (final AbstractPropertyColumnMapping mapping : mappings) {
	    if (propertyIsOfAbstractEntityType(entityClass, mapping.getPropertyName())) {
		mapping.setClickAction(new OpenMasterClickAction(entityMasterFactory, mapping.getPropertyName(), provider));
	    }
	}
    }

    /**
     * Returns true if property name represents either key of or entity itself, false otherwise.
     * 
     * @param entityClass
     * @param propertyName
     * @return
     */
    private static boolean propertyIsOfAbstractEntityType(final Class entityClass, String propertyName) {
	if (isEmpty(propertyName) || KEY.equals(propertyName)) {
	    // this means we should use containing entity
	    return true;
	}

	try {
	    propertyName = propertyName.endsWith("." + KEY) ? propertyName.substring(0, propertyName.length() - 4) : propertyName;
	    return AbstractEntity.class.isAssignableFrom(PropertyTypeDeterminator.determinePropertyType(entityClass, propertyName));
	} catch (final Exception e) {
	    return false;
	}
    }

}
