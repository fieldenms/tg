package ua.com.fielden.platform.web.test.server;

import com.google.inject.Injector;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;
import ua.com.fielden.platform.attachment.AttachmentUploader;
import ua.com.fielden.platform.sample.domain.stream_processors.DumpCsvTxtProcessor;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.application.AbstractWebUiResources;
import ua.com.fielden.platform.web.factories.webui.AttachmentDownloadResourceFactory;
import ua.com.fielden.platform.web.factories.webui.EgiExampleResourceFactory;
import ua.com.fielden.platform.web.factories.webui.FileProcessingResourceFactory;
import ua.com.fielden.platform.web.factories.webui.SerialisationTestResourceFactory;

/**
 * Custom {@link AbstractWebUiResources} descendant for Web UI Testing Server. Provided in order to configure entity centres, masters and other client specific stuff.
 *
 * @author TG Team
 *
 */
public class WebUiResources extends AbstractWebUiResources {

    /**
     * Creates an instance of {@link WebUiResources} (for more information about the meaning of all this arguments see {@link AbstractWebUiResources#AbstractWebApp}
     *
     * @param context
     * @param injector
     * @param resourcePaths
     * @param name
     * @param desc
     * @param owner
     * @param author
     * @param username
     */
    public WebUiResources(
            final Context context,
            final Injector injector,
            final String name,
            final String desc,
            final String owner,
            final String author,
            final IWebUiConfig webApp) {
        super(context, injector, name, desc, owner, author, webApp);
    }

    @Override
    protected void registerDomainWebResources(final Router router, final IWebUiConfig webApp) {
        // register some file processors
        final FileProcessingResourceFactory<DumpCsvTxtProcessor> factory = new FileProcessingResourceFactory<DumpCsvTxtProcessor>(
                webApp.getEventSourceEmitterRegister(),
                injector,
                DumpCsvTxtProcessor.class,
                f -> f.newByKey(DumpCsvTxtProcessor.class, "DUMMY"), // this entity construction could be more sophisticated in practice
                deviceProvider,
                dates,
                20 * 1024, // Kilobytes
                MediaType.TEXT_CSV,
                MediaType.TEXT_PLAIN);
        router.attach("/csv-txt-file-processing", factory);

        // register attachment uploader
        final FileProcessingResourceFactory<AttachmentUploader> factoryForAttachmentUploader = new FileProcessingResourceFactory<>(
                webApp.getEventSourceEmitterRegister(),
                injector,
                AttachmentUploader.class,
                f -> f.newEntity(AttachmentUploader.class),
                deviceProvider,
                dates,
                20 * 1024 * 1024, // Kilobytes
                // image/png,image/jpeg,
                // .csv,.txt,text/plain,text/csv,
                // application/pdf,application/zip,
                // application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,
                // application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
                MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG,
                MediaType.TEXT_CSV, MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_PDF, MediaType.APPLICATION_ZIP,
                MediaType.APPLICATION_WORD, MediaType.APPLICATION_MSOFFICE_DOCX,
                MediaType.APPLICATION_EXCEL, MediaType.APPLICATION_MSOFFICE_XLSX);
        router.attach("/upload-attachment", factoryForAttachmentUploader);

        // register attachment download resource
        router.attach("/download-attachment/{attachment-id}/{attachment-sha1}", new AttachmentDownloadResourceFactory(injector));

        // serialisation testing resource
        router.attach("/test/serialisation", new SerialisationTestResourceFactory(injector));
        // For egi example TODO remove later.
        router.attach("/test/egi", new EgiExampleResourceFactory(injector));

    }
}
