package ua.com.fielden.platform.serialisation;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

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
public class ServerEntityConverter extends AbstractEntityConverter {

    public ServerEntityConverter(final EntityFactory factory) {
        super(factory);
    }

    /**
     * Marshals entity passed as parameter <code>obj</code>.
     */
    @Override
    public void marshal(final Object obj, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final AbstractEntity<?> entity = (AbstractEntity) obj;
        // record entity id and type as attributes
        writer.addAttribute("id", entity.getId() != null ? entity.getId().toString() : "null");
        writer.addAttribute("ver", entity.getVersion().toString());
        // marshal properties -- only properties should be serialised; calculated properties should be ignored
        try {
            // iterate through properties
            final List<Field> properties = Finder.findRealProperties(entity.getType());
            for (final Field property : properties) {
                final String propertyName = property.getName();
                final Object propertyValue = entity.get(propertyName);
                if (!AnnotationReflector.isAnnotationPresent(property, Calculated.class)) {
                    if (propertyValue != null) {
                        // let's check if we're not operating on a Hibernate proxied collectional item
                        // TODO clean up - no hibernate proxies any longer
                        boolean isProxy = propertyValue.getClass().getName().endsWith("PersistentSet");
                        if (!isProxy && propertyValue instanceof AbstractEntity) {
                            try {
                                ((AbstractEntity) propertyValue).getVersion();
                            } catch (final Exception ex) {
                                isProxy = true;
                            }
                        }
                        if (!isProxy) {
                            converNotNullPropertyValue(writer, context, entity, propertyName, propertyValue, property);
                        } else {
                            converNullPropertyValue(writer, propertyName, property);
                        }
                    } else {
                        converNullPropertyValue(writer, propertyName, property);
                    }
                }
            }
        } catch (final Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Converts a property, which has a type derived from AbstractEntity and value of null.
     * 
     * @param writer
     * @param propertyName
     * @param metaProperty
     */
    private void converNullPropertyValue(final HierarchicalStreamWriter writer, final String propertyName, final Field property) {
        writer.startNode(propertyName);
        writer.addAttribute("isNull", "");
        writer.addAttribute("dirty", "false");
        writer.endNode();
    }

    /**
     * Implements conversion of a non-null property value.
     * 
     * @param writer
     * @param context
     * @param entity
     * @param propertyName
     * @param propertyValue
     * @param metaProperty
     */
    private void converNotNullPropertyValue(final HierarchicalStreamWriter writer, final MarshallingContext context, final AbstractEntity entity, final String propertyName, final Object propertyValue, final Field property) {
        if (PropertyDescriptor.class.isAssignableFrom(propertyValue.getClass())) {
            final AbstractEntity value = (AbstractEntity) propertyValue;
            writer.startNode(propertyName);
            writer.addAttribute("class", value.getType().getName());
            writer.addAttribute("to-string", value.toString());
            writer.addAttribute("dirty", "false");
            writer.endNode();
        } else if (AbstractEntity.class.isAssignableFrom(propertyValue.getClass())) {
            final AbstractEntity value = (AbstractEntity) propertyValue;
            writer.startNode(propertyName);
            writer.addAttribute("class", value.getType().getName());
            writer.addAttribute("dirty", "false");
            context.convertAnother(value);
            writer.endNode();
        } else if (Collection.class.isAssignableFrom(property.getType())
                && AbstractEntity.class.isAssignableFrom(AnnotationReflector.getAnnotation(property, IsProperty.class).value()) && ((Collection) propertyValue).size() > 0) {
            final Collection elements = (Collection) propertyValue;
            writer.startNode(propertyName);
            writer.addAttribute("class", elements.getClass().getName());
            writer.addAttribute("dirty", "false");
            for (final Object element : elements) {
                final AbstractEntity value = (AbstractEntity) element;
                writer.startNode("el");
                writer.addAttribute("class", value.getType().getName());
                context.convertAnother(value);
                writer.endNode();
            }
            writer.endNode();
        } else if (!"key".equals(propertyName) || !entity.hasCompositeKey()) {
            writer.startNode(propertyName);
            writer.addAttribute("class", propertyValue.getClass().getName());
            writer.addAttribute("dirty", "false");
            context.convertAnother(propertyValue);
            writer.endNode();
        }
    }

}
