package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.awt.event.ActionEvent;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.utils.Pair;

/**
 * Click action for {@link EntityGridInspector} that attempts to open master frame for the clicked cell if one represents an entity or entity's key.
 *
 * @author TG Team
 *
 */
public class OpenMasterClickAction extends BlockingLayerCommand<AbstractEntity<?>> {

    private static final long serialVersionUID = -641888610365274514L;

    private final String propertyName;

    private final String trimmedPropertyName;

    private final IEntityMasterManager entityMasterFactory;

    private final GridAnalysisView<?, ?> ownerView;

    /**
     * Principle constructor.
     *
     * @param entityMasterFactory
     *            -- the factory that knows all about entity masters
     * @param propertyName
     *            -- property name that gers associated with this action
     * @param ownerView
     *            -- an instance of {@link GridAnalysisView} that should be used as the owner of a corresponding entity master instance.
     * @param provider
     *            -- blocking layer provider that provides a way to block relevan UI components and display progress upon opening of entity masters.
     */
    private OpenMasterClickAction(final IEntityMasterManager entityMasterFactory, final String propertyName, final GridAnalysisView<?, ?> ownerView, final IBlockingLayerProvider provider) {
	super("Double-click facility", provider);
	this.propertyName = propertyName;
	this.entityMasterFactory = entityMasterFactory;
	this.ownerView = ownerView;
	// initialise trimmedPropertyName property that is used as the basis for invoking entity master
	this.trimmedPropertyName = propertyName.trim();

    }

    /**
     * Convenient constructor that crates the blocking layer provider based on the owner view.
     *
     * @param entityMasterFactory
     * @param propertyName
     * @param ownerView
     */
    private OpenMasterClickAction(final IEntityMasterManager entityMasterFactory, final String propertyName, final GridAnalysisView<?, ?> ownerView) {
	this(entityMasterFactory, propertyName, ownerView, new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return ownerView != null ? ownerView.getBlockingLayer() : null;
	    }

	});
    }

    /**
     * Convenient constructor for cases where the view owner is not available or not applicable.
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
    protected AbstractEntity<?> action(final ActionEvent e) throws Exception {
	if (!(e.getSource() instanceof AbstractEntity)) {
	    // it will always be not-null instance of AbstractEntity, but just to be sure
	    return null;
	}
	final Object clickedObject = getPropertyValue((AbstractEntity<?>) e.getSource());
	return clickedObject instanceof AbstractEntity ? (AbstractEntity<?>) clickedObject : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void postAction(final AbstractEntity<?> clickedEntity) {
	super.postAction(clickedEntity);
	if (clickedEntity != null) {
	    final AbstractEntity<?> originalClickedEntity = clickedEntity.copy((Class<AbstractEntity<?>>) DynamicEntityClassLoader.getOriginalType(clickedEntity.getType()));
	    entityMasterFactory.<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> showMaster(originalClickedEntity, ownerView);
	}
    }

    private Object getPropertyValue(final AbstractEntity<?> entity) {
	return isEmpty(getTrimmedPropertyName()) ? entity : entity.get(getTrimmedPropertyName());
    }

    public String getPropertyName() {
	return propertyName;
    }

    public String getTrimmedPropertyName() {
	return trimmedPropertyName;
    }

    /**
     * This factory method enhances {@link AbstractPropertyColumnMapping}s by associating property mappings with an instance of {@link OpenMasterClickAction}.
     *
     * @param mappings
     * @param entityClass
     * @param entityMasterFactory
     * @param ownerView
     */
    public static void enhanceWithClickAction(final Iterable<? extends AbstractPropertyColumnMapping<?>> mappings, final Class<?> entityClass, final IEntityMasterManager entityMasterFactory, final GridAnalysisView<?, ?> ownerView) {
	for (final AbstractPropertyColumnMapping<?> mapping : mappings) {
	    if (propertyIsOfAbstractEntityType(entityClass, mapping.getPropertyName())) {
		mapping.setClickAction(new OpenMasterClickAction(entityMasterFactory, mapping.getPropertyName(), ownerView));
	    } else {
		final String propertyOwner = findClosesEntity(entityClass, mapping.getPropertyName());
		mapping.setClickAction(new OpenMasterClickAction(entityMasterFactory, propertyOwner, ownerView));
	    }
	}
    }

    public static void enhanceWithBlockingLayer(final Iterable<? extends AbstractPropertyColumnMapping<?>> mappings, final Class<?> entityClass, final IEntityMasterManager entityMasterFactory, final IBlockingLayerProvider provider) {
	for (final AbstractPropertyColumnMapping<?> mapping : mappings) {
	    if (propertyIsOfAbstractEntityType(entityClass, mapping.getPropertyName())) {
		mapping.setClickAction(new OpenMasterClickAction(entityMasterFactory, mapping.getPropertyName(), provider));
	    } else {
		final String propertyOwner = findClosesEntity(entityClass, mapping.getPropertyName());
		mapping.setClickAction(new OpenMasterClickAction(entityMasterFactory, propertyOwner, provider));
	    }
	}
    }

    /**
     * Returns true if property name represents an entity.
     *
     * @param entityClass
     * @param propertyName
     * @return
     */
    private static boolean propertyIsOfAbstractEntityType(final Class<?> entityClass, final String propertyName) {
	if (isEmpty(propertyName) || KEY.equals(propertyName)) {
	    // this means we should use containing entity
	    return true;
	}

	try {
	    return AbstractEntity.class.isAssignableFrom(PropertyTypeDeterminator.determinePropertyType(entityClass, propertyName));
	} catch (final Exception e) {
	    return false;
	}
    }

    private static String findClosesEntity(final Class<?> entityClass, final String propertyName) {
	if (!PropertyTypeDeterminator.isDotNotation(propertyName)) {
	    return "";
	} else {
	    final Pair<String, String> res = PropertyTypeDeterminator.penultAndLast(propertyName);
	    final String candidate = res.getKey();
	    final Class<?> type = PropertyTypeDeterminator.determinePropertyType(entityClass, candidate);
	    if (AbstractEntity.class.isAssignableFrom(type)) {
		return candidate;
	    } else {
		return findClosesEntity(entityClass, candidate);
	    }
	}
    }

}
