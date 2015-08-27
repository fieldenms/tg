package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.resources.RestServerUtil;

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
    private final TgJackson tgJackson;

    public TgReflectorComponentResource(final RestServerUtil restUtil, final Context context, final Request request, final Response response, final TgJackson tgJackson) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.tgJackson = tgJackson;
    }

    /**
     * Handles sending of the serialised testing entities to the Web UI client (GET method).
     */
    @Override
    protected Representation get() throws ResourceException {
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(get(restUtil, tgJackson).getBytes(Charsets.UTF_8))));
    }

    public static String get(final RestServerUtil restUtil, final TgJackson tgJackson) {
        final String typeTableRepresentation = new String(restUtil.getSerialiser().serialise(tgJackson.getTypeTable(), SerialiserEngines.JACKSON), Charsets.UTF_8);
        final String text = ResourceLoader.getText("ua/com/fielden/platform/web/reflection/tg-reflector.html");

        return text.replace("@typeTable", typeTableRepresentation);
    }
}
