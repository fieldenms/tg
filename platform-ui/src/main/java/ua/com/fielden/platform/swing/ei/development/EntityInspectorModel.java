package ua.com.fielden.platform.swing.ei.development;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;

/**
 * TODO: Yura/Oleh should provide documentation for this class.
 *
 * @author Yura, Oleh
 * @author 01es
 *
 */
@SuppressWarnings("unchecked")
public class EntityInspectorModel<T extends AbstractEntity> {

    private final Logger logger = Logger.getLogger(getClass());

    private T entity;
    private final IPropertyBinder binder;

    private final Map<String, IPropertyEditor> editors;

    /**
     * Principle constructor.
     *
     * @param entity
     * @param binder
     */
    public EntityInspectorModel(final T entity, final IPropertyBinder binder) {
	final Date curr = new Date();
	logger.debug("Creating EntityInspectorModel (with binding)...");
	this.entity = entity;
	this.binder = binder;
	if (binder instanceof ILightweightPropertyBinder) {
	    editors = new HashMap<String, IPropertyEditor>();
	    ((ILightweightPropertyBinder) binder).rebind(editors, entity);
	} else {
	    editors = binder.bind(entity);
	}
	logger.debug("Creating EntityInspectorModel (with binding)...done in ..." + (new Date().getTime() - curr.getTime()) + "ms");
    }

    /**
     * Needed purely for internal purposes to support entity reloading.
     *
     * @param entity
     */
    protected void setEntity(final T entity) {
	this.entity = entity;
	if (binder instanceof ILightweightPropertyBinder) {
	    ((ILightweightPropertyBinder) binder).rebind(editors, entity);
	} else {
	    binder.bind(entity);
	}
    }

    public T getEntity() {
	return entity;
    }

    public Map<String, IPropertyEditor> getEditors() {
	return Collections.unmodifiableMap(editors);
    }

}
