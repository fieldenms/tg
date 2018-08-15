package ua.com.fielden.platform.attachment;

import static ua.com.fielden.platform.error.Result.failure;

import java.lang.reflect.Field;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T2;

public class AttachmentUtils {

    private AttachmentUtils() {}

    /**
     * Collects all properties of type {@link Attachment} from {@code entity} and processes their values by checking if attachments are persisted,
     * persisting them if necessary and reassigning persisted values to respective properties.
     *
     * @param entity
     */
    public static void persistAttachmentIfRequired(final AbstractEntity<?> entity, final IAttachment coAttachment) {
        final List<Field> attachmentProps = Finder.findPropertiesOfSpecifiedType(entity.getType(), Attachment.class, MapTo.class);
        attachmentProps.stream().map(f -> value(f, entity)).filter(t2 -> t2._2 != null && !t2._2.isPersisted())
        .forEach(t2 -> entity.set(t2._1, coAttachment.save(t2._2)));
    }

    private static T2<String, Attachment> value(final Field field, final AbstractEntity<?> entity) {
        field.setAccessible(true); 
        try {
            return T2.t2(field.getName(), (Attachment) field.get(entity));
        } catch (final IllegalArgumentException | IllegalAccessException ex) {
            throw failure(ex);
        }
    }

}
