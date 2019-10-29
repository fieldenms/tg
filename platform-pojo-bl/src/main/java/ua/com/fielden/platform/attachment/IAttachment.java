package ua.com.fielden.platform.attachment;

import java.io.File;
import java.util.Optional;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * A companion contract for {@link Attachment}.
 * 
 * @author TG Team
 * 
 */
public interface IAttachment extends IEntityDao<Attachment> {

    /**
     * Returns a file optionally. An empty result is returned if the identified by the attachment file could be located.
     * 
     * @param attachment
     * @return
     */
    Optional<File> asFile(final Attachment attachment);
    
    /**
     * Creates a new attachment that represents a hyperlink.
     * Returns an empty optional if {@code potentialUri} does not represent a valid URI.
     *
     * @param potentialUri
     * @return
     */
    Optional<Attachment> newAsHyperlink(final String potentialUri);
}
