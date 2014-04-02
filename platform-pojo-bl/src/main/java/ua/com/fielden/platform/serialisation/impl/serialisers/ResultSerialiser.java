package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializationException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.SimpleSerializer;
import com.esotericsoftware.kryo.serialize.StringSerializer;

/**
 * Serialises {@link Result} instances.
 * 
 * @author TG Team
 * 
 */
public class ResultSerialiser extends SimpleSerializer<Result> {

    private static final byte NULL = 1;
    private static final byte NOT_NULL = 2;

    private final Serializer mesageSerialiser;
    private final Kryo kryo;

    public ResultSerialiser(final Kryo kryo) {
        mesageSerialiser = new StringSerializer();
        this.kryo = kryo;
    }

    @Override
    public void write(final ByteBuffer buffer, final Result result) {
        // write information about the actual type of the result, which is required to recognise Warning and ordinary Result
        kryo.writeClass(buffer, result.getClass());
        // now persist the content of result
        buffer.put(result.getInstance() == null ? NULL : NOT_NULL);
        if (result.getInstance() != null) {
            kryo.writeClass(buffer, PropertyTypeDeterminator.stripIfNeeded(result.getInstance().getClass()));
            kryo.writeObject(buffer, result.getInstance());
        }
        buffer.put(result.getEx() == null ? NULL : NOT_NULL);
        if (result.getEx() != null) {
            kryo.writeObject(buffer, result.getEx().getMessage());
        }

        try {
            final Field messageField = Result.class.getDeclaredField("message");
            messageField.setAccessible(true);
            final Object msg = messageField.get(result);
            buffer.put(msg == null ? NULL : NOT_NULL);
            if (msg != null) {
                mesageSerialiser.writeObject(buffer, msg);
                //kryo.writeObject(buffer, msg);
            }
        } catch (final Exception e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public Result read(final ByteBuffer buffer) {
        final Class<?> resultType = kryo.readClass(buffer).getType();

        final Object instance;
        if (buffer.get() == NULL) {
            instance = null;
        } else {
            final Class<?> type = kryo.readClass(buffer).getType();
            instance = kryo.readObject(buffer, type);
        }
        final Exception ex = buffer.get() == NULL ? null : new Exception(mesageSerialiser.readObject(buffer, String.class));
        final String message = buffer.get() == NULL ? null : mesageSerialiser.readObject(buffer, String.class);

        // instantiate the result; warning type checking is required only when instance and message are not null
        if (ex != null && message == null) {
            return new Result(instance, ex);
        } else if (ex != null && message != null) {
            return new Result(instance, message, ex);
        } else {
            return Warning.class.equals(resultType) ? new Warning(instance, message) : new Result(instance, message);
        }
    }

}
