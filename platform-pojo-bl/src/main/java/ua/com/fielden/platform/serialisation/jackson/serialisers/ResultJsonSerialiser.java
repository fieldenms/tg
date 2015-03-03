package ua.com.fielden.platform.serialisation.jackson.serialisers;

import java.io.IOException;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serialiser for {@link Result} type.
 *
 * @author TG Team
 *
 */
public class ResultJsonSerialiser extends StdSerializer<Result> {
    private final TgJackson tgJackson;

    public ResultJsonSerialiser(final TgJackson tgJackson) {
        super(Result.class);
        this.tgJackson = tgJackson;
    }

    @Override
    public void serialize(final Result result, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeStartObject();

        generator.writeFieldName("@resultType");
        generator.writeObject(result.getClass().getName());
        generator.writeFieldName("message");
        generator.writeObject(result.getMessage());

        if (result.getInstance() != null) {
            generator.writeFieldName("@instanceType");
            final Class<?> type = PropertyTypeDeterminator.stripIfNeeded(result.getInstance().getClass()); // .getName();

            if (EntityUtils.isEntityType(type) && EntityQueryCriteria.class.isAssignableFrom(type)) {
                // TODO potentially extend support for generated types
                // TODO potentially extend support for generated types
                // TODO potentially extend support for generated types
                final Class<AbstractEntity<?>> newType = (Class<AbstractEntity<?>>) type;
                final Pair<Long, EntityType> numberAndType = tgJackson.registerNewType(newType);
                generator.writeObject(numberAndType.getValue());
            } else {
                generator.writeObject(type.getName());
            }

            generator.writeFieldName("instance");
            generator.writeObject(result.getInstance());
        }

        if (result.getEx() != null) {
            generator.writeFieldName("ex");
            generator.writeObject(result.getEx());
        }

        generator.writeEndObject();
    }
}
