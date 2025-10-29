package ua.com.fielden.platform.web.view.master.attachments;

import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;

import java.util.Optional;

import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public class AttachmentPreviewEntityMaster implements IMaster<AttachmentPreviewEntityAction> {

    private final IRenderable renderable;

    public AttachmentPreviewEntityMaster() {

        //Generating attachment preview element

        final DomElement attachmentPreview = new DomElement("tg-attachment-preview")
                .clazz("relative")
                .attr("id", "attachmentPreview")
                .attr("entity", "[[_currEntity]]")
                .style("width:100%", "height:100%");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, "import '/resources/file_operations/tg-attachment-preview.js'")
                .replace(ENTITY_TYPE, flattenedNameOf(AttachmentPreviewEntityAction.class))
                .replace("<!--@tg-entity-master-content-->", attachmentPreview.toString())
                .replace("//@ready-callback", generateReadyCallback())
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "true");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private String generateReadyCallback() {
        return  """
                self.$.attachmentPreview.downloadAttachment = self.mkDownloadAttachmentFunction();
                """;
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<AttachmentPreviewEntityAction, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting an action configuration is not supported.");
    }
}
