package ua.com.fielden.platform.sample.domain.observables;

import com.google.inject.Singleton;

import ua.com.fielden.platform.rx.AbstractSubjectKind;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

/**
 * This is an observable that should be used for publishing of change to entity {@link TgPersistentEntityWithProperties}, and should be used for subscribing to those events.
 *
 * @author TG Team
 *
 */
@Singleton
public class TgEntityForColourMasterChangeSubject extends AbstractSubjectKind<TgEntityForColourMaster> {

}
