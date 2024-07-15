package ua.com.fielden.platform.attachment;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.attachment.Attachment.pn_ORIG_FILE_NAME;
import static ua.com.fielden.platform.attachment.Attachment.pn_SHA1;

import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.RestrictCommasValidator;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Hyperlink;

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

    @Test
    public void file_names_may_contain_leading_trailing_sequential_whitespece_and_commas_resulting_in_titles_that_are_sanitised() {
        final String fileName = "  document  01, 02  .pdf  ";
        final String expectedTitle = "document 01 02 .pdf";
        final Attachment attachment = new_(Attachment.class).setSha1("AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4").setOrigFileName(fileName);
        final Result res = attachment.isValid();
        assertTrue(res.isSuccessful());
        assertEquals(fileName, attachment.getOrigFileName());
        assertEquals(expectedTitle, attachment.getTitle());
        assertEquals(format("%s | SHA1: AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4", expectedTitle), attachment.toString());
    }

    @Test
    public void hyperlink_attachment_created_with_newAsHyperlink_permits_commas_in_URLs() {
        final String url = "https://validdomainname.com/assets/Network/NIT/Dev/NT-AO-15-360%20Install%20a%20new%20Access%20Switch%20and%20connect%20to%20AH,BD,CD";
        final Result result = Hyperlink.validate(url);
        assertTrue(result.isSuccessful());

        final IAttachment coAttachment = co(Attachment.class);
        final Optional<Attachment> maybeAttachment = coAttachment.newAsHyperlink(url);
        assertTrue(maybeAttachment.isPresent());

        final Attachment attachment = maybeAttachment.get();
        final Result res = attachment.isValid();
        assertTrue(res.isSuccessful());

        assertEquals(Attachment.HYPERLINK, attachment.getOrigFileName());
        assertEquals(url, attachment.getTitle());
    }

    @Test
    public void hyperlink_attachment_created_manually_permits_commas_in_URLs() {
        final String url = "https://validdomainname.com/assets/Network/NIT/Dev/NT-AO-15-360%20Install%20a%20new%20Access%20Switch%20and%20connect%20to%20AH,BD,CD";
        final Result result = Hyperlink.validate(url);
        assertTrue(result.isSuccessful());

        final Attachment attachment = new_(Attachment.class);
        attachment.setTitle(url);
        final MetaProperty<String> mpTitle = attachment.getProperty(Attachment.pn_TITLE);
        assertFalse(mpTitle.isValid());
        assertEquals(RestrictCommasValidator.ERR_CONTAINS_COMMAS, mpTitle.getFirstFailure().getMessage());
        attachment.setOrigFileName(Attachment.HYPERLINK);
        assertTrue(mpTitle.isValid());

        assertEquals(Attachment.HYPERLINK, attachment.getOrigFileName());
        assertEquals(url, attachment.getTitle());
    }

    @Test
    public void hyperlink_attachment_created_manually_validates_URL() {
        final String invalidUrl = "https://validdomainname.com/assets/Network/NIT/Dev/NT-AO-15-360 Install a new Access%20Switch%20and%20connect%20to";
        final Result result = Hyperlink.validate(invalidUrl);
        assertFalse(result.isSuccessful());

        final Attachment attachment = new_(Attachment.class);
        attachment.setTitle(invalidUrl);
        final MetaProperty<String> mpTitle = attachment.getProperty(Attachment.pn_TITLE);
        assertTrue(mpTitle.isValid()); // not recognised as a hyperlink attachment yet, thus admitting invalidUrl
        attachment.setOrigFileName(Attachment.HYPERLINK); // now it becomes a hyperlink attachment and should revalidate the title
        assertFalse(mpTitle.isValid());
        assertEquals(format("Value [%s] is not a valid hyperlink.", invalidUrl), mpTitle.getFirstFailure().getMessage());
    }

    @Test
    public void file_attachments_do_not_permit_leading_trailing_sequential_whitespece_and_commas_in_titles() {
        final String fileName = "  document  01, 02  .pdf  ";
        final String expectedTitle = "document 01 02 .pdf";
        final Attachment attachment = new_(Attachment.class).setSha1("AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4").setOrigFileName(fileName);
        final Result res = attachment.isValid();
        assertTrue(res.isSuccessful());
        assertEquals(fileName, attachment.getOrigFileName());
        assertEquals(expectedTitle, attachment.getTitle());

        final MetaProperty<String> mpTitle = attachment.getProperty(Attachment.pn_TITLE);
        attachment.setTitle(" Title with leading space");
        assertFalse(mpTitle.isValid());
        assertEquals("Leading whitespace characters are not permitted: [{?}Title with leading space]", mpTitle.getFirstFailure().getMessage());
        attachment.setTitle("Title with trailing space ");
        assertEquals("Trailing whitespace characters are not permitted: [Title with trailing space{?}]", mpTitle.getFirstFailure().getMessage());
        attachment.setTitle("Title with consequtive   spaces");
        assertEquals("Consecutive whitespace characters are not permitted: [Title with consequtive{?}spaces]", mpTitle.getFirstFailure().getMessage());
        attachment.setTitle("Title with , commas");
        assertEquals(RestrictCommasValidator.ERR_CONTAINS_COMMAS, mpTitle.getFirstFailure().getMessage());
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        if (useSavedDataPopulationScript()) {
            return;
        }
    }

}
