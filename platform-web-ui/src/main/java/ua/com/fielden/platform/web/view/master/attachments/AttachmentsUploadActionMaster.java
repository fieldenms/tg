package ua.com.fielden.platform.web.view.master.attachments;

import java.util.LinkedHashSet;
import java.util.Optional;

import org.joda.time.DateTime;

import ua.com.fielden.platform.attachment.AttachmentsUploadAction;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * An entity master that represents a chart for {@link AttachmentsUploadAction}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class AttachmentsUploadActionMaster implements IMaster<AttachmentsUploadAction> {

    private final IRenderable renderable;

    public AttachmentsUploadActionMaster(final DateTime now) {

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("file_operations/tg-attachment-uploader-list");

        final DomElement attachmentUploaderList = new DomElement("tg-attachment-uploader-list")
                .attr("id", "attachmentUploader")
                .attr("entity", "[[_currBindingEntity]]")
                .attr("upload-size-limit-kb", "10240")
                .attr("mime-types-accepted", "image/png,image/jpeg,application/pdf,application/zip,.csv,.txt,text/plain,text/csv,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .attr("url", "/upload-attachment");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", AttachmentsUploadAction.class.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", attachmentUploaderList.toString())
                .replace("//generatedPrimaryActions", "")
                .replace("//@ready-callback", readyCallback())
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "false"); // true would save action upon retrieval, which is not what we need... I think...

        renderable = () -> new InnerTextElement(entityMasterStr);
    }

    private String readyCallback() {
        // that's a nice trick to include ... "self.classList.remove('canLeave');\n" ... if appropriate
        // TODO need to modify listeners to manage SAVE and CANCEL states
        return  "// register listeners for attachment uploading\n"
                + "const uploaderList = self.$.attachmentUploader;" 
                + "uploaderList.processUploadingStopped = function() {\n"
                + "    console.log('COMPLETED UPLOADING. Uploaded files:', uploaderList.numberOfUploaded, 'Attachments created:', uploaderList.attachments.length, 'Aborted files:', uploaderList.numberOfAborted, 'Failed files:', uploaderList.numberOfFailed);\n"
                + "    uploaderList.attachments.forEach( att => console.log('Attachment: id=', att.id, 'fileName:', att.origFileName, 'SHA1:', att.sha1) );\n"
                + "    this._toastGreeting().text = 'Uploading completed...';\n"
                + "    this._toastGreeting().hasMore = false;\n"
                + "    this._toastGreeting().showProgress = false;\n"
                + "    this._toastGreeting().msgHeading = 'Info';\n"
                + "    this._toastGreeting().isCritical = false;\n"
                + "    this._toastGreeting().show();\n"
                + "};\n"
                + "\n"
                + "uploaderList.processUploadingStarted = function(uploader) {\n"
                + "    console.log('STARTED UPLOADING of', uploader.fileName);\n"
                + "    this._toastGreeting().text = 'Started attachment uploading...';\n"
                + "    this._toastGreeting().hasMore = false;\n"
                + "    this._toastGreeting().showProgress = true;\n"
                + "    this._toastGreeting().msgHeading = 'Info';\n"
                + "    this._toastGreeting().isCritical = false;\n"
                + "    this._toastGreeting().show();\n"
                + "};\n";
    }

    @Override
    public IRenderable render() {
        return renderable;
    }


    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting an action configuration is not supported.");
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<AttachmentsUploadAction, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

}
