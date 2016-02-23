package ua.com.fielden.platform.persistence;

import java.util.Collections;

import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.def.DefaultPostLoadEventListener;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.DefinersExecutor;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This Hibernate listener executes meta-information updating while the instance of AbstractEntity's descendant loads. It executes meta-definers and updates "original-value". See
 * also {@link MetaInitializeCollectionListener}.
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
        DefinersExecutor.execute(instance);
        super.onPostLoad(event);
    }
}