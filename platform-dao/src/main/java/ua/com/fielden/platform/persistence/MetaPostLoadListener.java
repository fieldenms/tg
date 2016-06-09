package ua.com.fielden.platform.persistence;

import java.util.Collections;

import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.def.DefaultPostLoadEventListener;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This Hibernate listener executes meta-information updating while the instance of AbstractEntity's descendant loads. It executes meta-definers and updates "original-value". See
 * also {@link MetaInitializeCollectionListener}.
 * 
 * FIXME this Hibernate listener should be removed together with Hibernate loading of entity instances inside CommonEntityDao saving logic.
 * 
 * @author TG Team
 *
 */
public class MetaPostLoadListener extends DefaultPostLoadEventListener {
    private static final long serialVersionUID = -2573116334147311468L;

    @Override
    public void onPostLoad(final PostLoadEvent event) {
        final AbstractEntity<?> instance = (AbstractEntity<?>) event.getEntity();
        instance.beginInitialising();
        // TODO please note, that proxiedProps are passed empty, which means that definers on proxied by Hibernate
        // properties ($$_javassist) will be executed and, potentially will load them lazily
        EntityUtils.handleMetaProperties(instance, Collections.emptySet());
        instance.endInitialising();
        super.onPostLoad(event);
    }
}