package ua.com.fielden.platform.serialisation.xstream;

import java.util.Collection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This an XStream converter for marshaling and unmarshaling instances of {@link Result}.
 * 
 * @author 01es
 * 
 */
public class ResultConverter implements Converter {

    public ResultConverter() {
    }

    /**
     * Marshals entity passed as parameter <code>obj</code>.
     */
    @Override
    public void marshal(final Object obj, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Result result = (Result) obj;
        try {
            if (result.getEx() != null) {
                writer.addAttribute("ex", result.getEx().getMessage());
            } else if (result.getMessage() != null) {
                writer.addAttribute("msg", result.getMessage());
            }

            if (result.getInstance() instanceof Collection) {
                writer.startNode("instance"); //result.getInstance().getClass().getName()
                final Collection elements = (Collection) result.getInstance();
                writer.addAttribute("class", elements.getClass().getName());
                for (final Object element : elements) {
                    if (element != null) {
                        if (AbstractEntity.class.isAssignableFrom(element.getClass())) {
                            final AbstractEntity value = (AbstractEntity) element;
                            writer.startNode("el");
                            writer.addAttribute("class", value.getType().getName());
                        } else {
                            writer.startNode(element.getClass().getName());
                            //writer.addAttribute("class", elements.getClass().getName());
                        }

                        context.convertAnother(element);
                        writer.endNode();
                    }
                }
                writer.endNode();
            } else if (result.getInstance() != null && AbstractEntity.class.isAssignableFrom(result.getInstance().getClass())) {
                final AbstractEntity entity = (AbstractEntity) result.getInstance();
                writer.startNode("instance");
                writer.addAttribute("class", entity.getType().getName());
                context.convertAnother(entity);
                writer.endNode();
            } else if (result.getInstance() != null) {
                writer.startNode("instance");
                writer.addAttribute("class", result.getInstance().getClass().getName());
                context.convertAnother(result.getInstance());
                writer.endNode();
            }
        } catch (final Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        Object instancePropety = null;
        final String messagePropety = reader.getAttribute("msg");
        final Exception exProperty = reader.getAttribute("ex") != null ? new Exception(reader.getAttribute("ex")) : null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            final String propertyName = reader.getNodeName();
            if ("instance".equals(propertyName)) {
                try {
                    final Class<?> klass = Class.forName(reader.getAttribute("class"));
                    instancePropety = context.convertAnother(null, klass);
                } catch (final Exception ex) {
                    throw new ConversionException("Exception occurred during conversion:\n" + ex.getMessage());
                }

            }
            reader.moveUp();
        }

        return Warning.class.equals(context.getRequiredType()) ? new Warning(instancePropety, messagePropety) : new Result(instancePropety, messagePropety, exProperty);
    }

    @Override
    public boolean canConvert(final Class type) {
        return Result.class.equals(type) || Warning.class.equals(type);
    }
}
