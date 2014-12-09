package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.common.base.Charsets;

/**
 * Resource for tg-reflector component.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class TgReflectorComponentResource extends ServerResource {
    private final RestServerUtil restUtil;
    private final LinkedHashMap<Long, EntityType> typeTable;

    public TgReflectorComponentResource(final RestServerUtil restUtil, final Context context, final Request request, final Response response, final LinkedHashMap<Long, EntityType> typeTable) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.typeTable = typeTable;
    }

    /**
     * Handles sending of the serialised testing entities to the Web UI client (GET method).
     */
    @Override
    protected Representation get() throws ResourceException {
        final String typeTableRepresentation = new String(restUtil.getSerialiser().serialise(typeTable, SerialiserEngines.JACKSON), Charsets.UTF_8);
        final byte[] reflectorComponent = ResourceLoader.getText("ua/com/fielden/platform/web/reflection/tg-reflector.html").
                replaceAll("@typeTable", typeTableRepresentation).getBytes(Charsets.UTF_8);
        return RestServerUtil.encodedRepresentation(new ByteArrayInputStream(reflectorComponent), MediaType.TEXT_HTML);
    }
}
