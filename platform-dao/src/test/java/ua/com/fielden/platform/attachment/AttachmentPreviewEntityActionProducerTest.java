package ua.com.fielden.platform.attachment;

import org.junit.Test;
import ua.com.fielden.platform.attachment.producers.AttachmentPreviewEntityActionProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web.centre.CentreContext;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

/// Tests for [AttachmentPreviewEntityActionProducer], covering URI and [AttachmentPreviewEntityAction#kind()] derivation per attachment MIME.
///
public class AttachmentPreviewEntityActionProducerTest extends AbstractDaoTestCase {

    private AttachmentPreviewEntityAction produceAction(final Attachment attachment) {
        return getInstance(AttachmentPreviewEntityActionProducer.class)
                .setContext(new CentreContext<Attachment, AbstractEntity<?>>().setSelectedEntities(listOf(attachment)))
                .newEntity();
    }

    @Test
    public void image_attachment_yields_image_kind_and_plain_download_uri() {
        final Attachment attachment = save(new_(Attachment.class)
                .setSha1("AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4")
                .setOrigFileName("diagram.png")
                .setMime("image/png"));

        final AttachmentPreviewEntityAction action = produceAction(attachment);

        assertEquals(Optional.of(PreviewKind.IMAGE), action.kind());
        assertEquals("/download-attachment/" + attachment.getId() + "/" + attachment.getSha1(), action.getAttachmentUri());
    }

    @Test
    public void pdf_attachment_yields_pdf_kind_and_inline_flagged_download_uri() {
        final Attachment attachment = save(new_(Attachment.class)
                .setSha1("BD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4")
                .setOrigFileName("manual.pdf")
                .setMime("application/pdf"));

        final AttachmentPreviewEntityAction action = produceAction(attachment);

        assertEquals(Optional.of(PreviewKind.PDF), action.kind());
        assertEquals("/download-attachment/" + attachment.getId() + "/" + attachment.getSha1() + "?inline=true", action.getAttachmentUri());
    }

    @Test
    public void hyperlink_attachment_yields_hyperlink_kind_and_url_as_uri() {
        final String url = "https://example.com/page";
        final IAttachment coAttachment = co$(Attachment.class);
        final Attachment attachment = save(coAttachment.newAsHyperlink(url).orElseThrow());

        final AttachmentPreviewEntityAction action = produceAction(attachment);

        assertEquals(Optional.of(PreviewKind.HYPERLINK), action.kind());
        assertEquals(url, action.getAttachmentUri());
    }

    @Test
    public void attachment_with_unsupported_mime_yields_null_kind_and_null_uri() {
        final Attachment attachment = save(new_(Attachment.class)
                .setSha1("CD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4")
                .setOrigFileName("archive.zip")
                .setMime("application/zip"));

        final AttachmentPreviewEntityAction action = produceAction(attachment);

        assertEquals(Optional.empty(), action.kind());
        assertNull(action.getAttachmentUri());
    }

    @Test
    public void attachment_without_mime_yields_null_kind_and_null_uri() {
        final Attachment attachment = save(new_(Attachment.class)
                .setSha1("DD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4")
                .setOrigFileName("document.txt"));

        final AttachmentPreviewEntityAction action = produceAction(attachment);

        assertEquals(Optional.empty(), action.kind());
        assertNull(action.getAttachmentUri());
    }

    /// Pins the cross-file invariant that makes [PreviewKind] the single source of truth:
    /// the producer appends `?inline=true` to the download URL iff the resolved kind's [PreviewKind#servesInline] is `true`.
    /// The download resource consults the same predicate to decide `Disposition.TYPE_INLINE`, so if this test ever fails,
    /// the producer's URL construction and the resource's inline-disposition gate have drifted apart and the preview will silently break.
    ///
    /// Extend the sample list whenever a new previewable kind is added to [PreviewKind#fromMime].
    ///
    @Test
    public void producer_url_inline_flag_matches_kind_servesInline_contract() {
        final Attachment image = save(new_(Attachment.class)
                .setSha1("EA35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4")
                .setOrigFileName("diagram.png")
                .setMime("image/png"));
        final Attachment pdf = save(new_(Attachment.class)
                .setSha1("EB35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4")
                .setOrigFileName("manual.pdf")
                .setMime("application/pdf"));

        for (final Attachment attachment : listOf(image, pdf)) {
            final AttachmentPreviewEntityAction action = produceAction(attachment);
            final PreviewKind kind = action.kind().orElseThrow();
            final boolean urlHasInlineFlag = action.getAttachmentUri().endsWith("?inline=true");
            assertEquals(
                "URL inline flag must match " + kind + ".servesInline() for MIME " + attachment.getMime(),
                kind.servesInline(),
                urlHasInlineFlag);
        }
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

}
