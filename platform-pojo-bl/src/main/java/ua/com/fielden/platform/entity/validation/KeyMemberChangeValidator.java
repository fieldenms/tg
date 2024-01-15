package ua.com.fielden.platform.entity.validation;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;


/**
 * A validator for "key" and key-member properties of persistent entities that produces a warning upon value change.
 * Intended to be used on reference (aka master) data.
 *
 * @author TG Team
 */
public class KeyMemberChangeValidator extends AbstractBeforeChangeEventHandler<Object> {
    private static final Logger LOGGER = getLogger(KeyMemberChangeValidator.class);

    private final ICompanionObjectFinder coFinder;
    private final IApplicationDomainProvider applicationDomainProvider;

    @Inject
    public KeyMemberChangeValidator(final ICompanionObjectFinder coFinder, final IApplicationDomainProvider applicationDomainProvider) {
        this.coFinder = coFinder;
        this.applicationDomainProvider = applicationDomainProvider;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();
        if (!(entity instanceof ActivatableAbstractEntity<?> activEntity))
            return successful(newValue);
        if (!entity.isPersistent() || !entity.isPersisted())
            return successful(newValue);

        final int refCount;
        final IEntityDao<?> co = coFinder.find(entity.getType());
        if (!co.isStale(entity.getId(), entity.getVersion())) {
            refCount = activEntity.getRefCount();
        } else {
            // need to retrieve the latest refCount
            final fetch fetch = fetchOnly(entity.getType()).with("refCount");
            final ActivatableAbstractEntity<?> updatedEntity = (ActivatableAbstractEntity<?>) co.findById(entity.getId(), fetch);
            refCount = updatedEntity.getRefCount();
        }

        if (refCount > 0) {
            final String title = getEntityTitleAndDesc(entity.getType()).getKey();
            return warning(newValue, "Saving this change will be reflected in all the data referencing this [%s].".formatted(title));
        }

        return successful(newValue);
    }

}
