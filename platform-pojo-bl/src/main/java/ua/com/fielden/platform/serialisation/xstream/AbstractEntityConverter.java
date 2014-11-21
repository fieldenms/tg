package ua.com.fielden.platform.serialisation.xstream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * This an XStream converter for marshaling and unmarshaling {@link AbstractEntity} descendants.
 * 
 * TODO: In order to be able to reuse validation results performed by client at the server-end need to implement conversion of meta-properties. Specifically, need to marshal the
 * result of failed validation associated with each meta-property.
 * <p>
 * This converter is also responsible for marshaling the state <code>dirty</code>.
 * <p>
 * Another important converter feature is assignment of the original values to meta-properties during unmarshaling. Currently it is considered that the use of original values is
 * needed only at the client-side and thus any item coming from the server would have original and current property values equal at the unmarshling stage. It is however possible
 * that at some point in time it should be necessary to specifically marshal original values as well as current ones.
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractEntityConverter implements Converter {
    /**
     * Factory is essential for unmarshaling of {@link AbstractEntity} descendants.
     */
    private final EntityFactory factory;

    public AbstractEntityConverter(final EntityFactory factory) {
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final String type = reader.getAttribute("class") != null ? reader.getAttribute("class") : reader.getNodeName(); //getAttribute("type");
        final Long id = "null".equalsIgnoreCase(reader.getAttribute("id")) || reader.getAttribute("id") == null ? null : Long.parseLong(reader.getAttribute("id"));
        final Long ver = "null".equalsIgnoreCase(reader.getAttribute("ver")) || reader.getAttribute("ver") == null ? 0L : Long.parseLong(reader.getAttribute("ver"));
        try {
            final Class<? extends AbstractEntity<?>> entityType = "e".equals(type) ? context.getRequiredType() : (Class<? extends AbstractEntity<?>>) Class.forName(type);

            if (PropertyDescriptor.class.isAssignableFrom(entityType)) {
                final String to_string = reader.getAttribute("to-string");
                return PropertyDescriptor.fromString(to_string, factory);
            } else {

                final AbstractEntity entity = factory.newEntity(entityType, id);
                // set the version
                final Method setVersion = Reflector.getMethod(entityType, "setVersion", Long.class);
                setVersion.setAccessible(true);
                setVersion.invoke(entity, ver);

                // unmarshal entity properties
                entity.setInitialising(true); // switches entity into initialising mode
                while (reader.hasMoreChildren()) {
                    reader.moveDown();

                    final String propertyName = reader.getNodeName();
                    final boolean dirty = Boolean.valueOf(reader.getAttribute("dirty"));
                    final boolean isNull = reader.getAttribute("isNull") != null;
                    final String clazz = reader.getAttribute("class");

                    final Object propertyValue = isNull ? null : (StringUtils.isEmpty(clazz) ? context.convertAnother(entity, entity.getProperty(propertyName).getType())
                            : context.convertAnother(entity, Class.forName(clazz)));
                    final Field field = Finder.findFieldByName(entity.getType(), propertyName);
                    field.setAccessible(true);
                    field.set(entity, propertyValue);
                    // make current value an original
                    final MetaProperty property = entity.getProperty(propertyName);
                    property.setOriginalValue(propertyValue);
                    property.setDirty(dirty);

                    reader.moveUp();
                }

                // invoke meta definers for properties one all properties have been set
                for (final Object meta : entity.getProperties().values()) {
                    if (meta != null) {
                        final MetaProperty prop = (MetaProperty) meta;
                        if (!prop.isCollectional()) {
                            prop.define(prop.getOriginalValue());
                        }
                    }
                }

                entity.setInitialising(false); // switches off the initialising mode for an entity
                return entity;
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            throw new ConversionException("Exception occurred during conversion:\n" + ex.getMessage());
        }

    }

    @Override
    public final boolean canConvert(final Class type) {
        return AbstractEntity.class.isAssignableFrom(type);
    }
}
