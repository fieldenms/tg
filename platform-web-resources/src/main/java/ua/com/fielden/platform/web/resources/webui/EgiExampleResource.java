package ua.com.fielden.platform.web.resources.webui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Resource for integration test of Java and JavaScript serialisation.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class EgiExampleResource extends AbstractWebResource {
    private final RestServerUtil restUtil;
    private final List<AbstractEntity<?>> entities;

    public EgiExampleResource(final EntityFactory entityFactory, final RestServerUtil restUtil, final IDeviceProvider deviceProvider, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider);
        this.restUtil = restUtil;
        this.entities = createEntities(entityFactory);
    }

    /**
     * Handles sending of the serialised testing entities to the Web UI client (GET method).
     */
    @Get
    @Override
    public Representation get() {
        return restUtil.listJSONRepresentation(this.entities);
    }

    private static List<AbstractEntity<?>> createEntities(final EntityFactory entityFactory) {
        final List<AbstractEntity<?>> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add(entityFactory.newEntity(TgPersistentEntityWithProperties.class, (Long.valueOf(i)), "KEY" + i).setIntegerProp(i * 10 + 3).setStringProp("String prop value "
                    + i).setBigDecimalProp(BigDecimal.valueOf(i * 1.0 + 3.1))
                    .setDesc("Description for entity with key " + i + "."));
        }
        return data;
    }
}
