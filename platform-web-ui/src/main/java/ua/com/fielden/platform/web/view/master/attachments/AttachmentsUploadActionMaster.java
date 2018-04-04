package ua.com.fielden.platform.web.view.master.attachments;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.MOBILE;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.TABLET;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.REFRESH;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.SAVE;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.AttachmentsUploadAction;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.utils.StreamUtils;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.action.StandardMastersWebUiConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.DefaultEntityAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * An master for {@link AttachmentsUploadAction} that provides a way to:
 * <ul>
 * <li> Upload multiple files with ability to abort uploading.
 * <li> Automatically register uploaded files as attachments (i.e. instances of type {@link Attachment}) if they did not exist previously.
 * <li> Associate attachments that correspond to successfully uploaded files with an entity that is determined from the context (either master or selected entity).
 * </ul>
 * 
 * @author TG Team
 */
public class AttachmentsUploadActionMaster implements IMaster<AttachmentsUploadAction> {

    private final IRenderable renderable;
    private final FlexLayout actionBarLayout = new FlexLayout("actions");
    private final List<AttachmentsUploadActionMasterEntityActionConfig> entityActions = new ArrayList<>();
    

    public AttachmentsUploadActionMaster(final PrefDim dims, final int fileSizeLimitKb, final String mimeType, final String... moreMimeTypes) {
        final LinkedHashSet<String> importPaths = CollectionUtil.linkedSetOf(
                "file_operations/tg-attachment-uploader-list",
                "layout/tg-flex-layout",
                "master/actions/tg-action");

        final String mimeTypesAccepted = StreamUtils.of(mimeType, moreMimeTypes).collect(Collectors.joining(","));
        final DomElement attachmentUploaderList = new DomElement("tg-attachment-uploader-list")
                .attr("id", "attachmentUploader")
                .attr("class", "property-editors")
                .attr("entity", "[[_currBindingEntity]]")
                .attr("upload-size-limit-kb", fileSizeLimitKb)
                .attr("mime-types-accepted", mimeTypesAccepted)
                .attr("url", "/upload-attachment");
        
        addMasterAction(REFRESH).shortDesc("CANCEL").longDesc("Cancel attaching files");
        addMasterAction(SAVE).shortDesc("ATTACH").longDesc("Attach uploaded files");
        
        setActionBarLayoutFor(DESKTOP, Optional.empty(), mkActionLayoutForMaster());
        setActionBarLayoutFor(TABLET, Optional.empty(), mkActionLayoutForMaster());
        setActionBarLayoutFor(MOBILE, Optional.empty(), mkActionLayoutForMaster());
        
        final DomElement actionContainer = actionBarLayout.render().clazz("action-bar");
        final StringBuilder shortcuts = new StringBuilder();
        final StringBuilder entityActionsStr = new StringBuilder();
        for (final AttachmentsUploadActionMasterEntityActionConfig config : entityActions) {
            importPaths.add(config.action().importPath());
            if (config.action().shortcut() != null) {
                shortcuts.append(config.action().shortcut() + " ");
            }
            if (config.action() instanceof IRenderable) {
                actionContainer.add(((IRenderable) config.action()).render());
            }
            if (config.action() instanceof IExecutable) {
                entityActionsStr.append(((IExecutable) config.action()).code().toString());
            }
        }
        
        final DomElement elementContainer = new DomContainer().add(attachmentUploaderList, actionContainer);
        
        final StringBuilder prefDimBuilder = new StringBuilder();
        prefDimBuilder.append(format("{'width': function() {return %s}, 'height': function() {return %s}, 'widthUnit': '%s', 'heightUnit': '%s'}", dims.width, dims.height, dims.widthUnit.value, dims.heightUnit.value));
        
        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", AttachmentsUploadAction.class.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", elementContainer.toString())
                .replace("//generatedPrimaryActions", "")
                .replace("//@ready-callback", 
                         actionBarLayout.code().toString() + "\n" + 
                         entityActionsStr.toString() + "\n" + 
                         uploaderListEventHandling())
                .replace("@SHORTCUTS", shortcuts)
                .replace("@prefDim", prefDimBuilder.toString())
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "false"); // true would save action upon retrieval, which is not what we need... I think...

        renderable = () -> new InnerTextElement(entityMasterStr);
    }

    private String uploaderListEventHandling() {
        return  "// Overridden to support hidden property conversion on the client-side ('attachmentIds').\n"
                + "self._isNecessaryForConversion = function (propertyName) {\n"
                + "    return ['attachmentIds'].indexOf(propertyName) !== -1;\n" 
                + "};\n"
                + "// register listeners for attachment uploading\n"
                + "const uploaderList = self.$.attachmentUploader;\n"
                + "self.addEventListener('binding-entity-appeared', () => {\n"
                + "    if (self._currEntity['chosenPropName']) {\n"
                + "        uploaderList.multi = false;\n"
                + "    }\n"
                + "});\n"
                // when uploading start, master needs to go to status VIEW and kick in a toast with generic progress.
                + "uploaderList.processUploadingStarted = function(uploader) {\n"
                + "    //console.log('STARTED UPLOADING of', uploader.fileName);\n"
                + "    uploaderList.classList.add('canLeave');\n"
                + "    self.view();\n"
                + "    self._toastGreeting().text = 'Uploading files...';\n"
                + "    self._toastGreeting().hasMore = false;\n"
                + "    self._toastGreeting().showProgress = true;\n"
                + "    self._toastGreeting().msgHeading = 'Info';\n"
                + "    self._toastGreeting().isCritical = false;\n"
                + "    self._toastGreeting().show();\n"
                + "};\n"
                // when uploading stops, master needs to go to status EDIT and kick in a toast with generic progress.
                + "uploaderList.processUploadingStopped = function() {\n"
                + "    //console.log('COMPLETED UPLOADING. Uploaded files:', uploaderList.numberOfUploaded, 'Attachments created:', uploaderList.attachments.length, 'Aborted files:', uploaderList.numberOfAborted, 'Failed files:', uploaderList.numberOfFailed);\n"
                + "    //uploaderList.attachments.forEach( att => console.log('Attachment: id=', att.id, 'fileName:', att.origFileName, 'SHA1:', att.sha1) );\n"
                + "    const ids = uploaderList.attachments.map(att => att.id);\n"
                + "    self._currBindingEntity.setAndRegisterPropertyTouch('attachmentIds', ids);\n"
                + "    uploaderList.classList.remove('canLeave');\n"                
                + "    self.edit();\n"                
                + "    self._toastGreeting().text = 'Uploaded ' + uploaderList.numberOfUploaded +' / Failed ' + uploaderList.numberOfFailed +' / Aborted ' + uploaderList.numberOfAborted;\n"
                + "    self._toastGreeting().hasMore = false;\n"
                + "    self._toastGreeting().showProgress = false;\n"
                + "    self._toastGreeting().msgHeading = 'Info';\n"
                + "    self._toastGreeting().isCritical = false;\n"
                + "    self._toastGreeting().show();\n"
                + "};\n";
                
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    protected AttachmentsUploadActionMasterEntityActionConfig addMasterAction(final MasterActions masterAction) {
        final DefaultEntityAction defaultEntityAction = new DefaultEntityAction(masterAction.name(), SimpleMasterBuilder.getPostAction(masterAction), SimpleMasterBuilder.getPostActionError(masterAction));
        final Optional<String> shortcut = SimpleMasterBuilder.getShortcut(masterAction);
        if (shortcut.isPresent()) {
            defaultEntityAction.setShortcut(shortcut.get()); // default value of shortcut if present
        }
        final Optional<String> focusingCallback = SimpleMasterBuilder.getFocusingCallback(masterAction);
        if (focusingCallback.isPresent()) {
            defaultEntityAction.setFocusingCallback(focusingCallback.get()); // default value of focusingCallback if present
        }
        final AttachmentsUploadActionMasterEntityActionConfig entityAction = new AttachmentsUploadActionMasterEntityActionConfig(defaultEntityAction, this);
        entityActions.add(entityAction);
        return entityAction;
    }

    protected AttachmentsUploadActionMaster setActionBarLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null) {
            throw new IllegalArgumentException("Device and orientation (optional) are required for specifying the layout.");
        }
        actionBarLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }

    private static String mkActionLayoutForMaster() {
        final String MARGIN_PIX_FOR_MASTER_ACTION = "10px";
        final String MASTER_ACTION_LAYOUT_SPECIFICATION = "'horizontal', 'padding: " + MARGIN_PIX_FOR_MASTER_ACTION + "', 'wrap', 'justify-content: center',";
        final String MASTER_ACTION_SPECIFICATION = StandardMastersWebUiConfig.MASTER_ACTION_SPECIFICATION;
        
        final StringBuilder layout = new StringBuilder();
        layout.append("[").append(MASTER_ACTION_LAYOUT_SPECIFICATION).append(",[").append(MASTER_ACTION_SPECIFICATION).append("],[").append(MASTER_ACTION_SPECIFICATION).append("]]");
        return layout.toString();
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
