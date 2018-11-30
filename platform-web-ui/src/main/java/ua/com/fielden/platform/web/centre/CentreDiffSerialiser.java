package ua.com.fielden.platform.web.centre;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import ua.com.fielden.platform.serialisation.exceptions.SerialisationException;

/**
 * Serialiser for centre diffs.
 * 
 * @author TG Team
 *
 */
class CentreDiffSerialiser extends ObjectMapper {
    private static final long serialVersionUID = 1L;
    public static final CentreDiffSerialiser CENTRE_DIFF_SERIALISER = new CentreDiffSerialiser();
    
    private CentreDiffSerialiser() {
    }
    
    /**
     * Serialises centre <code>diff</code>.
     * 
     * @param diff
     * @return
     */
    public byte[] serialise(final Map<String, Object> diff) {
        try {
            final byte[] bytes = writeValueAsBytes(diff); // default encoding is Charsets.UTF_8
            return bytes;
        } catch (final JsonProcessingException ex) {
            throw new SerialisationException("Error during centre diff serialisation.", ex);
        }
    }
    
    /**
     * Deserialises centre diff from <code>diffBytes</code>.
     * 
     * @param diffBytes
     * @return
     */
    public Map<String, Object> deserialise(final byte[] diffBytes) {
        try {
            final ByteArrayInputStream bis = new ByteArrayInputStream(diffBytes);
            final String contentString = IOUtils.toString(bis, "UTF-8");
            final JavaType concreteType = getTypeFactory().constructType(LinkedHashMap.class);
            final Map<String, Object> deserialised = readValue(contentString, concreteType);
            return deserialised;
        } catch (final IOException e) {
            throw new SerialisationException("Error during centre diff deserialisation.", e);
        }
    }
    
}