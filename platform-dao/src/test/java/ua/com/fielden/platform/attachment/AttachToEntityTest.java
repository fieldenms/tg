package ua.com.fielden.platform.attachment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.ITgCategory;
import ua.com.fielden.platform.sample.domain.ITgCategoryAttachment;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgCategoryAttachment;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * A test case to cover the functionality of associating persisted and non-persisted {@link Attachment} instances with entities supporting association with attachments.
 */
public class AttachToEntityTest  extends AbstractDaoTestCase {

    @Test
    public void attaching_non_persisted_hyperlink_attachment_to_entity_auto_saves_it_during_entity_saving() {
        final ITgCategoryAttachment co$CatAttach = co$(TgCategoryAttachment.class);
        final IAttachment co$Attachment = co$(Attachment.class);
        final String uri = "https://supported.uri.org";
        final Attachment hyperAttachment = co$Attachment.findByKeyAndFetch(co$CatAttach.getFetchProvider().<Attachment>fetchFor("attachment").fetchModel(), uri);
        assertNotNull(hyperAttachment);
        assertFalse(hyperAttachment.isPersisted());
        
        final ITgCategory coCat = co(TgCategory.class);
        final TgCategory cat = coCat.findByKeyAndFetch(coCat.getFetchProvider().fetchModel(), "CAT1");
        assertNotNull(cat);
        
        final TgCategoryAttachment catAttach = save(co$CatAttach.new_().setAttachedTo(cat).setAttachment(hyperAttachment));
        
        assertTrue(catAttach.isPersisted());
        assertTrue(catAttach.getAttachment().isPersisted());
        assertEquals(hyperAttachment, catAttach.getAttachment());
    }

    @Test
    public void attaching_persisted_hyperlink_attachment_to_entity_is_supported() {
        final ITgCategoryAttachment co$CatAttach = co$(TgCategoryAttachment.class);
        final IAttachment co$Attachment = co$(Attachment.class);
        final String uri = "https://supported.uri.org";
        final Attachment hyperAttachment = save(co$Attachment.findByKeyAndFetch(co$CatAttach.getFetchProvider().<Attachment>fetchFor("attachment").fetchModel(), uri));
        assertNotNull(hyperAttachment);
        assertTrue(hyperAttachment.isPersisted());
        
        final ITgCategory coCat = co(TgCategory.class);
        final TgCategory cat = coCat.findByKeyAndFetch(coCat.getFetchProvider().fetchModel(), "CAT1");
        assertNotNull(cat);
        
        final TgCategoryAttachment catAttach = save(co$CatAttach.new_().setAttachedTo(cat).setAttachment(hyperAttachment));
        
        assertTrue(catAttach.isPersisted());
        assertTrue(catAttach.getAttachment().isPersisted());
        assertEquals(hyperAttachment, catAttach.getAttachment());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
       if (useSavedDataPopulationScript()) {
           return;
       }
        
       save(new_(TgCategory.class, "CAT1").setActive(true));
    }

}
