package ua.com.fielden.platform.sample.domain.observables;

import com.google.inject.Singleton;

import ua.com.fielden.platform.rx.AbstractSubjectKind;
import ua.com.fielden.platform.sample.domain.TgMessage;

/**
 * This is an observable that should be used for publishing of change to entity {@link TgMessage},
 * and should be used for subscribing to those events.
 *
 * @author TG Team
 *
 */
@Singleton
public class TgMessageChangeSubject extends AbstractSubjectKind<TgMessage> {

}
