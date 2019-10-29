package ua.com.fielden.platform.attachment;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.attachment.Attachment.pn_ORIG_FILE_NAME;
import static ua.com.fielden.platform.attachment.Attachment.pn_SHA1;

import org.junit.Test;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * A test case validating basic properties of attachments.
 * 
 * @author TG Team
 * 
 */
public class AttachmentBasicPropertiesTest extends AbstractDaoTestCase {

    @Test
    public void origFileName_can_only_be_assigned_once() {
        final Attachment attachment = new_(Attachment.class).setSha1("AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4").setOrigFileName("document_01.pdf");
        assertTrue(attachment.isValid().isSuccessful());
        
        attachment.setOrigFileName("some_other_document_name.pdf");
        assertFalse(attachment.isValid().isSuccessful());
        final MetaProperty<String> origFileNameProp = attachment.getProperty(pn_ORIG_FILE_NAME);
        final Result validation = origFileNameProp.validationResult();
        assertFalse(validation.isSuccessful());
        assertEquals(format(Final.ERR_REASSIGNMENT, origFileNameProp.getTitle(), Attachment.ENTITY_TITLE),  validation.getMessage());
    }

    @Test
    public void sha1_can_only_be_assigned_once() {
        final Attachment attachment = new_(Attachment.class).setSha1("AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4").setOrigFileName("document_01.pdf");
        assertTrue(attachment.isValid().isSuccessful());
        
        attachment.setSha1("CD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertFalse(attachment.isValid().isSuccessful());
        final MetaProperty<String> sha1Prop = attachment.getProperty(pn_SHA1);
        final Result validation = sha1Prop.validationResult();
        assertFalse(validation.isSuccessful());
        assertEquals(format(Final.ERR_REASSIGNMENT, sha1Prop.getTitle(), Attachment.ENTITY_TITLE),  validation.getMessage());
    }

    @Test
    public void title_is_set_to_origFileName_by_default_if_it_is_empty() {
        final String origFileName = "document_01.pdf";
        final String sha1 = "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4";
        
        final Attachment attachment = new_(Attachment.class).setSha1(sha1).setOrigFileName(origFileName);
        assertEquals(origFileName, attachment.getTitle());
        assertEquals(origFileName, attachment.getOrigFileName());
        
        final Attachment anotherAttachment = new_(Attachment.class).setTitle("Document title").setSha1(sha1).setOrigFileName(origFileName);
        assertEquals("Document title", anotherAttachment.getTitle());
        assertEquals(origFileName, anotherAttachment.getOrigFileName());
    }
    
    @Test
    public void attachment_string_representation_consists_of_title_and_SHA1_delimited_with_SHA1_keyword() {
        final Attachment attachment = new_(Attachment.class).setSha1("AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4").setOrigFileName("document_01.pdf");
        assertEquals("document_01.pdf | SHA1: AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4", attachment.toString());
        assertEquals("alternative title | SHA1: AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4", attachment.setTitle("alternative title").toString());
    }

    @Test
    public void attachments_are_created_with_rev_0_and_no_last_or_prev_revisions() {
        final Attachment attachment = new_(Attachment.class).setSha1("AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4").setOrigFileName("document_01.pdf");
        assertEquals(Integer.valueOf(0), attachment.getRevNo());
        assertNull(attachment.getLastRevision());
        assertNull(attachment.getPrevRevision());
        
        final Attachment savedAttachment = save(attachment); 
        assertEquals(Integer.valueOf(0), savedAttachment.getRevNo());
        assertNull(savedAttachment.getLastRevision());
        assertNull(savedAttachment.getPrevRevision());
    }

}