package ua.com.fielden.platform.web.test.eventsources;

import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.observables.TgMessageChangeSubject;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.sse.AbstractEventSource;

/**
 * Event source to notify changes to {@link TgMessage}.
 *
 * @author TG Team
 *
 */
public class TgMessageEventSource extends AbstractEventSource<TgMessage, TgMessageChangeSubject> {
    private final IDates dates;

    @Inject
    protected TgMessageEventSource(final TgMessageChangeSubject observableKind, final IDates dates, final ISerialiser serialiser) {
        super(observableKind, serialiser);
        this.dates = dates;
    }

    @Override
    protected Map<String, Object> eventToData(final TgMessage event) {
        return mapOf(t2("id", event.getMachine().getId()), t2("key", event.getKey()), t2("changeDate", dates.now().toDate()));
    }

}