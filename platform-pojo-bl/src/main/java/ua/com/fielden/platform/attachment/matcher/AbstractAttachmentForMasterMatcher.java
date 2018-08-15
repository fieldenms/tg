package ua.com.fielden.platform.attachment.matcher;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * This is a base matcher that creates and persists if valid a new attachment-as-hyperlink {@link Attachment} instance is case where no matches were found.
 * It should be overridden to specify a concrete context-entity and be for autocompleters on all masters that represent properties of type {@link Attachment}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAttachmentForMasterMatcher<C extends AbstractEntity<?>> extends AbstractSearchEntityByKeyWithContext<C, Attachment> {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final IAttachment coAttachment;
    
    @Inject
    public AbstractAttachmentForMasterMatcher (final IAttachment coAttachment) {
        super(coAttachment);
        this.coAttachment = coAttachment;
    }

    @Override
    public List<Attachment> findMatches(final String searchString) {
        return findMatchesWithModel(searchString);
    }
    
    @Override
    public List<Attachment> findMatchesWithModel(final String searchString) {
        final List<Attachment> matched = super.findMatchesWithModel(searchString);
        // if no matches found then let's check if an ad-hoc hyperlink-as-attachment needs to be created
        if (matched.isEmpty()) {
            final String potentialUri = searchString.replaceAll("%$", ""); // remove the last % is present
            coAttachment.newAsHyperlink(potentialUri).ifPresent(matched::add);
        }
        return matched;
    }
}