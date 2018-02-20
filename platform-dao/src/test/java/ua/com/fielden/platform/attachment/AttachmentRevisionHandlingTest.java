package ua.com.fielden.platform.attachment;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.attachment.Attachment.pn_LAST_REVISION;
import static ua.com.fielden.platform.attachment.Attachment.pn_PREV_REVISION;

import org.junit.Test;

import ua.com.fielden.platform.attachment.validators.CanBeUsedAsLastAttachmentRev;
import ua.com.fielden.platform.attachment.validators.CanBeUsedAsPrevAttachmentRev;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case handling of revision tracking for attachments.
 * 
 * @author TG Team
 * 
 */
public class AttachmentRevisionHandlingTest extends AbstractDaoTestCase {
    private final fetch<Attachment> fmAttachment = co(Attachment.class).getFetchProvider().fetchModel();

    @Test
    public void attachments_are_created_with_rev_0() {
        assertEquals(Integer.valueOf(0), co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4").getRevNo());
        assertEquals(Integer.valueOf(0), co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C").getRevNo());
        assertEquals(Integer.valueOf(0), co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C").getRevNo());
        assertEquals(Integer.valueOf(0), co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_04.pdf", "D5B3FF9137053279C8B1ECE96F817BA0129F614C").getRevNo());
        assertEquals(Integer.valueOf(0), co(Attachment.class).findByKeyAndFetch(fmAttachment, "duplicate_of_document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C").getRevNo());
    }
    
    @Test
    public void attachment_cannot_reference_itself_as_prev_revision() {
        // use document 01 as prev. revision for itself
        final Attachment document01 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertNull(document01.getPrevRevision());
        assertEquals(Integer.valueOf(0), document01.getRevNo());
        
        document01.setPrevRevision(document01);
        
        // let's assert the validation result
        assertFalse(document01.isValid().isSuccessful());
        final MetaProperty<Attachment> mpPrevRevision = document01.getProperty(pn_PREV_REVISION);
        
        assertFalse(mpPrevRevision.validationResult().isSuccessful());
        assertEquals(format(CanBeUsedAsPrevAttachmentRev.ERR_SELF_REFERENCE, mpPrevRevision.getTitle()), mpPrevRevision.validationResult().getMessage());
    }

    @Test
    public void rebasing_revision_history_is_not_permitted() {
        // use document 01 as prev. revision for document 02
        final Attachment document01 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertNull(document01.getPrevRevision());
        assertEquals(Integer.valueOf(0), document01.getRevNo());
        
        final Attachment document02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(document02.getPrevRevision());
        assertEquals(Integer.valueOf(0), document02.getRevNo());
        
        save(document02.setPrevRevision(document01));
        
        // let's now attempt to use document 01 also as a prev. revision for document 03, which should not be permitted
        final Attachment document03 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        final Attachment document01Refetched = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        document03.setPrevRevision(document01Refetched);
        
        assertFalse(document03.isValid().isSuccessful());
        final MetaProperty<Attachment> mpPrevRevision = document03.getProperty(pn_PREV_REVISION);
        
        assertFalse(mpPrevRevision.validationResult().isSuccessful());
        assertEquals(CanBeUsedAsPrevAttachmentRev.ERR_REBASING_REVISION, mpPrevRevision.validationResult().getMessage());
    }

    @Test
    public void attachment_cannot_reference_itself_as_last_revision_when_there_is_no_revision_history() {
        // use document 01 as prev. revision for itself
        final Attachment document01 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertNull(document01.getPrevRevision());
        assertEquals(Integer.valueOf(0), document01.getRevNo());
        
        document01.setLastRevision(document01);
        
        // let's assert the validation result
        assertFalse(document01.isValid().isSuccessful());
        final MetaProperty<Attachment> mpLastRevision = document01.getProperty(pn_LAST_REVISION);
        
        assertFalse(mpLastRevision.validationResult().isSuccessful());
        assertEquals(format(CanBeUsedAsLastAttachmentRev.ERR_SELF_REFERENCE, mpLastRevision.getTitle()), mpLastRevision.validationResult().getMessage());
    }

    @Test
    public void assigning_prev_revision_updates_rev_no_to_value_greater_by_1_to_that_of_prev_revision() {
        final Attachment doc01 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertNull(doc01.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc01.getRevNo());
        
        final Attachment doc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc02.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc02.getRevNo());
        
        final Attachment doc02NextRevForDoc01 = save(doc02.setPrevRevision(doc01));
        final Attachment doc01PrevRevForDoc02 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        
        assertEquals(Integer.valueOf(1), doc02NextRevForDoc01.getRevNo());
        assertEquals(doc01PrevRevForDoc02, doc02NextRevForDoc01.getPrevRevision());
        assertEquals(doc02NextRevForDoc01, doc02NextRevForDoc01.getLastRevision());
        
        assertEquals(Integer.valueOf(0), doc01PrevRevForDoc02.getRevNo());
        assertNull(doc01PrevRevForDoc02.getPrevRevision());
        assertEquals(doc02NextRevForDoc01, doc01PrevRevForDoc02.getLastRevision());
    }

    @Test
    public void last_revision_cannot_be_empty_in_the_presence_of_revision_history() {
        final Attachment doc01 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertNull(doc01.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc01.getRevNo());
        
        final Attachment doc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc02.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc02.getRevNo());
        
        final Attachment doc02NextRevForDoc01 = save(doc02.setPrevRevision(doc01));
        final Attachment doc01PrevRevForDoc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        
        // try emptying the last revision, which should not be permitted
        doc01PrevRevForDoc02.setLastRevision(null);
        
        // let's assert the validation result
        assertFalse(doc01PrevRevForDoc02.isValid().isSuccessful());
        final MetaProperty<Attachment> mpLastRevisionDoc01 = doc01PrevRevForDoc02.getProperty(pn_LAST_REVISION);
        
        assertFalse(mpLastRevisionDoc01.validationResult().isSuccessful());
        assertEquals(format(CanBeUsedAsLastAttachmentRev.ERR_LAST_REV_CANNOT_BE_EMPTY, mpLastRevisionDoc01.getTitle()), mpLastRevisionDoc01.validationResult().getMessage());
        
        // try emptying the last revision, which should not be permitted
        doc02NextRevForDoc01.setLastRevision(null);
        
        // let's assert the validation result
        assertFalse(doc02NextRevForDoc01.isValid().isSuccessful());
        final MetaProperty<Attachment> mpLastRevisionDoc02 = doc02NextRevForDoc01.getProperty(pn_LAST_REVISION);
        
        assertFalse(mpLastRevisionDoc02.validationResult().isSuccessful());
        assertEquals(format(CanBeUsedAsLastAttachmentRev.ERR_LAST_REV_CANNOT_BE_EMPTY, mpLastRevisionDoc02.getTitle()), mpLastRevisionDoc02.validationResult().getMessage());
    }

    @Test
    public void reassigning_last_revision_to_self_that_leads_to_broken_revision_histor_is_not_permitted() {
        final Attachment doc01 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertNull(doc01.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc01.getRevNo());
        
        final Attachment doc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc02.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc02.getRevNo());
        
        final Attachment doc02NextRevForDoc01 = save(doc02.setPrevRevision(doc01));
        final Attachment doc01PrevRevForDoc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        
        // try reassigning self as the last revision, which should not be permitted
        doc01PrevRevForDoc02.setLastRevision(doc01PrevRevForDoc02);
        
        // let's assert the validation result
        assertFalse(doc01PrevRevForDoc02.isValid().isSuccessful());
        final MetaProperty<Attachment> mpLastRevision = doc01PrevRevForDoc02.getProperty(pn_LAST_REVISION);
        
        assertFalse(mpLastRevision.validationResult().isSuccessful());
        assertEquals(format(CanBeUsedAsLastAttachmentRev.ERR_SELF_REFERENCE, mpLastRevision.getTitle()), mpLastRevision.validationResult().getMessage());
    }

    @Test
    public void reassigning_last_revision_to_another_attachment_that_leads_to_broken_revision_histor_is_not_permitted() {
        final Attachment doc01 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertNull(doc01.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc01.getRevNo());
        
        final Attachment doc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc02.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc02.getRevNo());
        
        final Attachment doc02NextRevForDoc01 = save(doc02.setPrevRevision(doc01));
        final Attachment doc01PrevRevForDoc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        
        // try reassigning self as the last revision, which should not be permitted
        final Attachment doc03 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        doc01PrevRevForDoc02.setLastRevision(doc03);
        
        // let's assert the validation result
        assertFalse(doc01PrevRevForDoc02.isValid().isSuccessful());
        final MetaProperty<Attachment> mpLastRevision = doc01PrevRevForDoc02.getProperty(pn_LAST_REVISION);
        
        assertFalse(mpLastRevision.validationResult().isSuccessful());
        assertEquals(CanBeUsedAsLastAttachmentRev.ERR_REBASING_REVISION, mpLastRevision.validationResult().getMessage());
    }

    @Test
    public void updating_revision_history_longer_than_one_revision_from_tail_end_correctly_rectifies_rev_numbers_and_last_revisions() {
        // let's make a revision history by specifying doc03 as prev. revision for doc04
        final Attachment doc03 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc03.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc03.getRevNo());
        
        final Attachment doc04 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_04.pdf", "D5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc04.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc04.getRevNo());
        
        final Attachment doc04NextRevForDoc03 = save(doc04.setPrevRevision(doc03));
        final Attachment doc03PrevRevForDoc04 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        
        // let's now make another revision update from tail-end by specifying doc02 as prev. revision for doc03
        final Attachment doc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc02.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc02.getRevNo());
        
        final Attachment doc03PrevRevForDoc04Updated = save(doc03PrevRevForDoc04.setPrevRevision(doc02));
        final Attachment doc02PrevRevForDoc03 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        
        // assert a complete attachment revision history
        final Attachment doc04Complete = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_04.pdf", "D5B3FF9137053279C8B1ECE96F817BA0129F614C");
        final Attachment doc03Complete = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        final Attachment doc02Complete = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        
        assertEquals(Integer.valueOf(2), doc04Complete.getRevNo());
        assertEquals(doc04Complete, doc04Complete.getLastRevision());
        assertEquals(doc03Complete, doc04Complete.getPrevRevision());
        
        assertEquals(Integer.valueOf(1), doc03Complete.getRevNo());
        assertEquals(doc04Complete, doc03Complete.getLastRevision());
        assertEquals(doc02Complete, doc03Complete.getPrevRevision());
        
        assertEquals(Integer.valueOf(0), doc02Complete.getRevNo());
        assertEquals(doc04Complete, doc02Complete.getLastRevision());
        assertNull(doc02Complete.getPrevRevision());
    }

    @Test
    public void joining_two_revision_histories_correctly_rectifies_rev_numbers_and_last_revisions() {
        // let's make one revision history by specifying doc01 as prev. revision for doc02
        final Attachment doc01 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        assertNull(doc01.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc01.getRevNo());
        
        final Attachment doc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc02.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc02.getRevNo());
        
        final Attachment doc02NextRevForDoc01 = save(doc02.setPrevRevision(doc01));
        final Attachment doc01PrevRevRefetched = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        
        // let's now make another revision history by specifying doc03 as prev. revision for doc04
        final Attachment doc03 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc03.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc03.getRevNo());
        
        final Attachment doc04 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_04.pdf", "D5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc04.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc04.getRevNo());
        
        final Attachment doc04NextRevForDoc03 = save(doc04.setPrevRevision(doc03));
        final Attachment doc03PrevRevForDoc04 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        
        // and now let's join two revision histories
        save(doc03PrevRevForDoc04.setPrevRevision(doc02NextRevForDoc01));

        // assert a complete attachment revision history
        final Attachment doc04Complete = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_04.pdf", "D5B3FF9137053279C8B1ECE96F817BA0129F614C");
        final Attachment doc03Complete = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        final Attachment doc02Complete = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        final Attachment doc01Complete = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_01.pdf", "AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4");
        
        
        assertEquals(Integer.valueOf(3), doc04Complete.getRevNo());
        assertEquals(doc04Complete, doc04Complete.getLastRevision());
        assertEquals(doc03Complete, doc04Complete.getPrevRevision());
        
        assertEquals(Integer.valueOf(2), doc03Complete.getRevNo());
        assertEquals(doc04Complete, doc03Complete.getLastRevision());
        assertEquals(doc02Complete, doc03Complete.getPrevRevision());
        
        assertEquals(Integer.valueOf(1), doc02Complete.getRevNo());
        assertEquals(doc04Complete, doc02Complete.getLastRevision());
        assertEquals(doc01Complete, doc02Complete.getPrevRevision());
        
        assertEquals(Integer.valueOf(0), doc01Complete.getRevNo());
        assertEquals(doc04Complete, doc01Complete.getLastRevision());
        assertNull(doc01Complete.getPrevRevision());
    }

    @Test
    public void shallow_revision_history_does_not_permit_duplicates_which_are_determined_by_document_SHA1_checksum() {
        final Attachment doc03 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc03.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc03.getRevNo());
        
        final Attachment doc03dup = co(Attachment.class).findByKeyAndFetch(fmAttachment, "duplicate_of_document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc03dup.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc03dup.getRevNo());
        
        
        doc03.setPrevRevision(doc03dup);

        // let's assert the validation result
        assertFalse(doc03.isValid().isSuccessful());
        final MetaProperty<Attachment> mpPrevRevision = doc03.getProperty(pn_PREV_REVISION);
        
        assertFalse(mpPrevRevision.validationResult().isSuccessful());
        assertEquals(format(CanBeUsedAsPrevAttachmentRev.ERR_DUPLICATE_SHA1, doc03.getSha1()), mpPrevRevision.validationResult().getMessage());
    }
    
    @Test
    public void deep_revision_history_does_not_permit_duplicates_which_are_determined_by_document_SHA1_checksum() {
        // let's make a revision history by specifying doc03 as prev. revision for doc04
        final Attachment doc03 = co(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc03.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc03.getRevNo());
        
        final Attachment doc04 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_04.pdf", "D5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc04.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc04.getRevNo());
        
        final Attachment doc04NextRevForDoc03 = save(doc04.setPrevRevision(doc03));
        final Attachment doc03PrevRevForDoc04 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        
        // let's now make another revision update from tail-end by specifying doc02 as prev. revision for doc03
        final Attachment doc02 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc02.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc02.getRevNo());
        
        final Attachment doc03PrevRevForDoc04Updated = save(doc03PrevRevForDoc04.setPrevRevision(doc02));
        final Attachment doc02PrevRevForDoc03 = co$(Attachment.class).findByKeyAndFetch(fmAttachment, "document_02.pdf", "B5B3FF9137053279C8B1ECE96F817BA0129F614C");

        // let's now try making a revision with a duplicate document, which should not be allowed
        final Attachment doc03dup = co(Attachment.class).findByKeyAndFetch(fmAttachment, "duplicate_of_document_03.pdf", "C5B3FF9137053279C8B1ECE96F817BA0129F614C");
        assertNull(doc03dup.getPrevRevision());
        assertEquals(Integer.valueOf(0), doc03dup.getRevNo());

        try {
            // have to try to save the updated attachment to enforce duplicate tracing in deep revision history
            save(doc02PrevRevForDoc03.setPrevRevision(doc03dup));
        } catch (final Result res) {
            assertEquals(format(CanBeUsedAsPrevAttachmentRev.ERR_DUPLICATE_SHA1, doc03dup.getSha1()), res.getMessage());
        }
    }

    
    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2018-02-01 00:00:00"));

        save(new_(Attachment.class).setSha1("AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4").setOrigFileName("document_01.pdf"));
        save(new_(Attachment.class).setSha1("B5B3FF9137053279C8B1ECE96F817BA0129F614C").setOrigFileName("document_02.pdf"));
        save(new_(Attachment.class).setSha1("C5B3FF9137053279C8B1ECE96F817BA0129F614C").setOrigFileName("document_03.pdf"));
        save(new_(Attachment.class).setSha1("D5B3FF9137053279C8B1ECE96F817BA0129F614C").setOrigFileName("document_04.pdf"));
        save(new_(Attachment.class).setSha1("C5B3FF9137053279C8B1ECE96F817BA0129F614C").setOrigFileName("duplicate_of_document_03.pdf"));
    }
}