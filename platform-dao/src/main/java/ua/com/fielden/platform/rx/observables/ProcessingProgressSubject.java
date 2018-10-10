package ua.com.fielden.platform.rx.observables;

import ua.com.fielden.platform.rx.AbstractSubjectKind;

/**
 * This is an observable of a generic nature that should be used for publishing of progress of some long running processing task.
 * 
 * It is not a singleton, which means that each client subscription should get its own copy.
 *
 * @author TG Team
 *
 */
public class ProcessingProgressSubject extends AbstractSubjectKind<Integer> {

}
