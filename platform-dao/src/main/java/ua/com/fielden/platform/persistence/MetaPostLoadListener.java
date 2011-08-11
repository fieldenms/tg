package ua.com.fielden.platform.persistence;

import java.util.Arrays;

import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.def.DefaultPostLoadEventListener;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * This Hibernate listener executes meta-information updating while the instance
 * of AbstractEntity's descendant loads. It executes meta-definers and updates
 * "original-value". See also{@link MetaInitializeCollectionListener}.
 *
 * @author Jhou
 *
 */
public class MetaPostLoadListener extends DefaultPostLoadEventListener {
    private static final long serialVersionUID = -2573116334147311468L;

    @Override
    public void onPostLoad(final PostLoadEvent event) {
	final AbstractEntity<?> instance = (AbstractEntity<?>) event.getEntity();
	//	System.out.println("<<< >>> onPostLoad : entity == " + instance.getId() + "(" + instance.getType().getSimpleName() + ")");

	final Object[] state = instance.getState();
	final String[] propertyNames = instance.getPropertyNames();

	// handle property "key" assignment
	final int keyIndex = Arrays.asList(propertyNames).indexOf("key");
	if (keyIndex >= 0 && state[keyIndex] != null) {
	    instance.set("key", state[keyIndex]);
	}

	for (final MetaProperty meta : instance.getProperties().values()) {
	    if (meta != null) {
		final Object newOriginalValue = instance.get(meta.getName());
		meta.setOriginalValue(newOriginalValue);
		if (!meta.isCollectional()) {
		    meta.define(newOriginalValue);
		}
	    }
	}
	instance.setDirty(false);
	super.onPostLoad(event);
    }
}
