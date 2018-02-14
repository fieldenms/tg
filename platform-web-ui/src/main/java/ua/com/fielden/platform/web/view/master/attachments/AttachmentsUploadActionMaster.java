package ua.com.fielden.platform.web.view.master.attachments;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.PrefDim.mkDim;

import java.util.LinkedHashSet;
import java.util.Optional;

import org.joda.time.DateTime;

import ua.com.fielden.platform.attachment.AttachmentsUploadAction;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.PrefDim.Unit;
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

    public AttachmentsUploadActionMaster() {

        final LinkedHashSet<String> importPaths = CollectionUtil.linkedSetOf(
                "file_operations/tg-attachment-uploader-list",
                "layout/tg-flex-layout",
                "master/actions/tg-action");

        final DomElement attachmentUploaderList = new DomElement("tg-attachment-uploader-list")
                .attr("id", "attachmentUploader")
                .attr("class", "property-editors")
                .attr("entity", "[[_currBindingEntity]]")
                .attr("upload-size-limit-kb", "10240")
                .attr("mime-types-accepted", "image/png,image/jpeg,application/pdf,application/zip,.csv,.txt,text/plain,text/csv,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .attr("url", "/upload-attachment");

        final PrefDim dims = mkDim(400, Unit.PX, 400, Unit.PX);
        final StringBuilder prefDimBuilder = new StringBuilder();
        prefDimBuilder.append(format("{'width': function() {return %s}, 'height': function() {return %s}, 'widthUnit': '%s', 'heightUnit': '%s'}", dims.width, dims.height, dims.widthUnit.value, dims.heightUnit.value));
        
        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", AttachmentsUploadAction.class.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", attachmentUploaderList.toString())
                .replace("//generatedPrimaryActions", "")
                .replace("//@ready-callback", readyCallback())
                .replace("@prefDim", prefDimBuilder.toString())
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "false"); // true would save action upon retrieval, which is not what we need... I think...

        renderable = () -> new InnerTextElement(entityMasterStr);
    }

    private String readyCallback() {
        // that's a nice trick to include ... "self.classList.remove('canLeave');\n" ... if appropriate
        // TODO need to modify listeners to manage SAVE and CANCEL states
        return  "// Overridden to support hidden property conversion on the client-side ('attachments').\n"
                +"const editorContainer = self.$.masterDom.getEditorContainer();\n"
                + "if (editorContainer) {\n"
                + "    editorContainer.style.display = 'flex';\n"
                + "}\n"
                + "self._isNecessaryForConversion = function (propertyName) {\n"
                + "    return ['attachmentIds'].indexOf(propertyName) !== -1;\n" 
                + "};\n"
                + "// register listeners for attachment uploading\n"
                + "const uploaderList = self.$.attachmentUploader;" 
                + "uploaderList.processUploadingStopped = function() {\n"
                + "    console.log('COMPLETED UPLOADING. Uploaded files:', uploaderList.numberOfUploaded, 'Attachments created:', uploaderList.attachments.length, 'Aborted files:', uploaderList.numberOfAborted, 'Failed files:', uploaderList.numberOfFailed);\n"
                + "    uploaderList.attachments.forEach( att => console.log('Attachment: id=', att.id, 'fileName:', att.origFileName, 'SHA1:', att.sha1) );\n"
                + "    const ids = uploaderList.attachments.map(att => att.id);\n"
                + "    self._currBindingEntity.setAndRegisterPropertyTouch('attachmentIds', ids);\n"
                + "    self._toastGreeting().text = 'Uploaded ' + uploaderList.numberOfUploaded +' / Failed ' + uploaderList.numberOfFailed +' / Aborted ' + uploaderList.numberOfAborted;\n"
                + "    self._toastGreeting().hasMore = false;\n"
                + "    self._toastGreeting().showProgress = false;\n"
                + "    self._toastGreeting().msgHeading = 'Info';\n"
                + "    self._toastGreeting().isCritical = false;\n"
                + "    self._toastGreeting().show();\n"
                + "    self.save();\n"
                + "};\n"
                + "\n"
                + "uploaderList.processUploadingStarted = function(uploader) {\n"
                + "    console.log('STARTED UPLOADING of', uploader.fileName);\n"
                + "    self._toastGreeting().text = 'Uploading files...';\n"
                + "    self._toastGreeting().hasMore = false;\n"
                + "    self._toastGreeting().showProgress = true;\n"
                + "    self._toastGreeting().msgHeading = 'Info';\n"
                + "    self._toastGreeting().isCritical = false;\n"
                + "    self._toastGreeting().show();\n"
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
