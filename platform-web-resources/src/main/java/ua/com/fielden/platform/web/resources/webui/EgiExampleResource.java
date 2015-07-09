package ua.com.fielden.platform.web.resources.webui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Resource for integration test of Java and JavaScript serialisation.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class EgiExampleResource extends ServerResource {
    private final RestServerUtil restUtil;
    private final List<AbstractEntity<?>> entities;

    public EgiExampleResource(final EntityFactory entityFactory, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.entities = createEntities(entityFactory);
    }

    /**
     * Handles sending of the serialised testing entities to the Web UI client (GET method).
     */
    @Override
    protected Representation get() throws ResourceException {
        return restUtil.listJSONRepresentation(this.entities);
    }

    private static List<AbstractEntity<?>> createEntities(final EntityFactory entityFactory) {
        final List<AbstractEntity<?>> data = new ArrayList<AbstractEntity<?>>();
        for (int i = 0; i < 100; i++) {
            data.add(entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY" + i).setIntegerProp(i * 10 + 3).setStringProp("String prop value " + i).setBigDecimalProp(BigDecimal.valueOf(i * 1.0 + 3.1))
                    .setDesc("Description for entity with key " + i + "."));
        }
        return data;
    }
}
