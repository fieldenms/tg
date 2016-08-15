package ua.com.fielden.platform.types;

import static java.lang.String.format;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.types.exceptions.ValueObjectException;

/**
 * A type for representing hyper links such as <code>http://.
 * 
 * @author TG Team
 *
 */
public class Hyperlink {
    // http://, https://, ftp://, ftps:// and mailto:
    // file:// protocol should not be permitted due to restrictions of modern web browsers to open local resources
    
    public enum SupportedProtocols {
        HTTP("http://"),
        HTTPS("https://"),
        FTP("ftp://"),
        FTPS("ftps://"),
        MAILTO("mailto:");
        
        public final String protocol;
        
        private SupportedProtocols(final String protocol) {
            this.protocol = protocol;
        }
        
        public static Optional<SupportedProtocols> identify(final String hyperlink){
            for(final SupportedProtocols v: values()){
                if( hyperlink.startsWith(v.protocol)){
                    return Optional.of(v);
                }
            }
            return Optional.empty();
        }
        
    }
    
    public final String value;
    
    public Hyperlink(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(final String value) {
        if (StringUtils.isBlank(value)) {
            throw new ValueObjectException("The hyperlink protocol canno be blank.");
        }
        
        final Optional<SupportedProtocols> protocol = SupportedProtocols.identify(value); 
        if (!protocol.isPresent()) {
            throw new ValueObjectException(format("Value [%s] for a hyperlink does specify any of the supported protocols.", value));
        }
        
    }
    
    @Override
    public int hashCode() {
        return 31 * value.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof Hyperlink)) {
            return false;
        }
        
        final Hyperlink that = (Hyperlink) obj;

        return this.value.equals(that.value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
