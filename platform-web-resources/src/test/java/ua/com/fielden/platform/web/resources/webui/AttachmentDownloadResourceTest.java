package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.AttachmentUploader;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.web.resources.webui.AttachmentDownloadResource.*;

/// Tests for the response of [AttachmentDownloadResource].
///
public class AttachmentDownloadResourceTest extends AbstractWebResourceWithDaoTestCase {

    @Inject private RestServerUtil restUtil;
    @Inject private IDeviceProvider deviceProvider;
    @Inject private IDates dates;
    @Inject @Named("attachments.location") private String attachmentsLocation;

    private final List<Path> uploadedFiles = new ArrayList<>();

    @Before
    public void createAttachmentsLocation() throws IOException {
        Files.createDirectories(Path.of(attachmentsLocation));
    }

    @After
    public void deleteUploadedFiles() throws IOException {
        for (final Path file : uploadedFiles) {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void PDF_served_inline_restricts_the_content_with_nosniff_and_a_content_security_policy() {
        final var attachment = upload("document.pdf", "%PDF-1.4\n%%EOF\n");

        final var response = new Response(downloadRequest(attachment, true));
        final Representation file = download(response);
        assertEquals(Disposition.TYPE_INLINE, file.getDisposition().getType());
        assertEquals(X_CONTENT_TYPE_OPTIONS, response.getHeaders().getFirstValue(HEADER_X_CONTENT_TYPE_OPTIONS));
        assertEquals(CONTENT_SECURITY_POLICY, response.getHeaders().getFirstValue(HEADER_CONTENT_SECURITY_POLICY));
    }

    @Test
    public void file_served_as_attachment_restricts_the_content_with_nosniff_and_a_content_security_policy() {
        final var attachment = upload("notes.txt", "Notes.");

        final var response = new Response(downloadRequest(attachment, false));
        final Representation file = download(response);
        assertEquals(Disposition.TYPE_ATTACHMENT, file.getDisposition().getType());
        assertEquals(X_CONTENT_TYPE_OPTIONS, response.getHeaders().getFirstValue(HEADER_X_CONTENT_TYPE_OPTIONS));
        assertEquals(CONTENT_SECURITY_POLICY, response.getHeaders().getFirstValue(HEADER_CONTENT_SECURITY_POLICY));
    }

    private Attachment upload(final String fileName, final String content) {
        final var uploader = new_(AttachmentUploader.class);
        uploader.setOrigFileName(fileName).setLastModified(dates.now().toDate()).setInputStream(new ByteArrayInputStream(content.getBytes(UTF_8)));
        final Attachment attachment = co$(AttachmentUploader.class).save(uploader).getKey();
        uploadedFiles.add(Path.of(attachmentsLocation, attachment.getSha1()));
        return attachment;
    }

    private static Request downloadRequest(final Attachment attachment, final boolean inline) {
        final var request = new Request(Method.GET, "http://localhost/download_attachment/%s/%s%s".formatted(attachment.getId(), attachment.getSha1(), inline ? "?inline=true" : ""));
        request.getAttributes().put("attachment-id", attachment.getId().toString());
        request.getAttributes().put("attachment-sha1", attachment.getSha1());
        return request;
    }

    private Representation download(final Response response) {
        return new AttachmentDownloadResource(restUtil, co(Attachment.class), deviceProvider, dates, new Context(), response.getRequest(), response).download();
    }

}
