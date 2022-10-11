package ua.com.fielden.platform.web.rx.eventsources;

import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;

import java.util.Map;

import ua.com.fielden.platform.rx.observables.ProcessingProgressSubject;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.sse.AbstractEventSource;

/**
 * This is a generic event source that should be used to update on events from subject of type {@link ProcessingProgressSubject}.
 *
 * @author TG Team
 *
 */
public class ProcessingProgressEventSource extends AbstractEventSource<Integer, ProcessingProgressSubject> {

    private final String jobUid;

    public ProcessingProgressEventSource(final ProcessingProgressSubject observableKind, final String jobUid, final ISerialiser serialiser) {
        super(observableKind, serialiser);
        this.jobUid = jobUid;
    }

    @Override
    protected Map<String, Object> eventToData(final Integer event) {
        return mapOf(t2("prc", event), t2("jobUid", jobUid));
    }

}