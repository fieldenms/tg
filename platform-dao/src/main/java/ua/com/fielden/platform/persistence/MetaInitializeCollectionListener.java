package ua.com.fielden.platform.persistence;

import org.hibernate.HibernateException;
import org.hibernate.event.InitializeCollectionEvent;
import org.hibernate.event.def.DefaultInitializeCollectionEventListener;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * THIS SHOULD BE REMOVED -- initialisation of TG collections does not use Hibernate loading
 * <p> 
 * This Hibernate listener executes meta-information updating while the instance's collection initializes (when the proxy becomes initialized collection). It executes meta-definers
 * and updates "original-value".
 * 
 * @author TG Team
 * 
 */
@Deprecated
public class MetaInitializeCollectionListener extends DefaultInitializeCollectionEventListener {
    private static final long serialVersionUID = -8089450033770556813L;

    @Override
    public void onInitializeCollection(final InitializeCollectionEvent event) throws HibernateException {
        final AbstractEntity<?> instance = (AbstractEntity<?>) event.getAffectedOwnerOrNull();
        //	System.out.println("<<< >>> onInitializeCollection : entity == " + instance.getId() + "(" + instance.getType().getSimpleName() + ") ");
        for (final MetaProperty meta : instance.getProperties().values()) {
            if (meta.isCollectional()) {
                final Object collection = instance.get(meta.getName());
                if (collection != null && collection == event.getCollection()) { //
                    final String hql = "select count(elements(entity." + meta.getName() + ")) from " + instance.getType().getSimpleName() + " entity where entity.id = "
                            + instance.getId();
                    System.out.println("QUERY (determines collection size) : " + hql);
                    final Number collSize = (Number) event.getSession().createQuery(hql).uniqueResult();
                    if (collSize == null) {
                        throw new RuntimeException("The collection size failed to determine before collection initialization.");
                    }
                    meta.setCollectionOriginalValue(collSize);
                    meta.define(collection);
                    super.onInitializeCollection(event);
                    return;
                }
            }
        }
        super.onInitializeCollection(event);
    }
}