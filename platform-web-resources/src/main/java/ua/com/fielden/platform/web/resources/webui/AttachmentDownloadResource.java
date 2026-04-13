package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import java.util.Set;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.lang.String.format;
import static ua.com.fielden.platform.attachment.Attachment.pn_SHA1;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

/// This resource should be used for downloading files that are associated with [Attachment] instances.
///
/// By default, the response uses a `TYPE_ATTACHMENT` content disposition, which causes browsers to offer the
/// file as a download. Clients that want the browser to render the file inline (e.g. a PDF quick preview) can pass
/// `?inline=true`. The flag is honoured only for MIME types listed in [#INLINE_SAFE_MIME_TYPES]; for any
/// other type the file is still served as an attachment, so untrusted user-uploaded content with executable payloads
/// (HTML, SVG with scripts, JavaScript, etc.) cannot be rendered in the application's origin.
///
public class AttachmentDownloadResource extends AbstractWebResource {
    private static final Logger LOGGER = LogManager.getLogger(AttachmentDownloadResource.class);

    /// MIME types that are safe to render inline in the browser when requested via `?inline=true`.
    /// Anything not on this list is served as a downloadable attachment regardless of the flag.
    ///
    /// The allow-list exists to prevent execution of script-carrying content (HTML, SVG with scripts, JavaScript,
    /// XML, etc.) in the application's origin via a user-uploaded attachment.
    ///
    private static final Set<String> INLINE_SAFE_MIME_TYPES = Set.of(MediaType.APPLICATION_PDF.getName());

    private final RestServerUtil restUtil;
    private final IAttachment coAttachment;
    private final Long attachmentId;
    private final String attachmentSha1;
    private final boolean inline;

    public AttachmentDownloadResource(
            final RestServerUtil restUtil,
            final IAttachment coAttachment,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.restUtil = restUtil;
        this.coAttachment = coAttachment;
        this.attachmentId = Long.parseLong(request.getAttributes().get("attachment-id").toString());
        this.attachmentSha1 = request.getAttributes().get("attachment-sha1") + "";
        this.inline = "true".equalsIgnoreCase(request.getResourceRef().getQueryAsForm().getFirstValue("inline"));
    }

    @Get
    public Representation download() {
        return handleUndesiredExceptions(getResponse(), () -> {
            LOGGER.debug(format("Looking for attachment [ID = %s, SHA1=%s]", attachmentId, attachmentSha1));
            final EntityResultQueryModel<Attachment> query = select(Attachment.class).where().prop(ID).eq().val(attachmentId).and().prop(pn_SHA1).eq().val(attachmentSha1).model();
            return coAttachment.getEntityOptional(from(query).with(coAttachment.getFetchProvider().fetchModel()).model())
            .map(attachment -> {
                LOGGER.debug(format("Attachment [SHA1=%s] was found, getting the associated file.", attachmentId));
                return coAttachment.asFile(attachment)
                .map(file -> {                
                    LOGGER.debug(format("Preparing and sending the response with file for attachment [ID=%s].", attachmentId));
                    final MediaType fileType = MediaType.valueOf(attachment.getMime());
                    final Disposition disposition = new Disposition();
                    disposition.setType(inline && INLINE_SAFE_MIME_TYPES.contains(fileType.getName())
                            ? Disposition.TYPE_INLINE
                            : Disposition.TYPE_ATTACHMENT);
                    disposition.setFilename(urlPathSegmentEscaper().escape(attachment.getOrigFileName()));
                    disposition.setModificationDate(attachment.getLastModified());

                    final Representation repFile = new FileRepresentation(file, fileType);
                    repFile.setDisposition(disposition);
                    return repFile;
                }).orElseThrow(() -> failure(format("Could not read file for attachment [ID = %s, SHA1=%s, %s]", attachmentId, attachmentSha1, attachment)));
            }).orElseThrow(() -> failure(format("Attachment [ID = %s, SHA1=%s] could not be found.", attachmentId, attachmentSha1)));
        }, restUtil);
    }
}
