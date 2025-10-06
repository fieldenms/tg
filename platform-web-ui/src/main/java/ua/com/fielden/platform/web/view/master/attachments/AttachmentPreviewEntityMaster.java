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

        //Generating image element

        final DomElement img = new DomElement("img")
                .clazz("relative")
                .attr("src$", "[[_getImageUri(_currBindingEntity)]]")
                .attr("hidden$", "[[!_isImageVisible(_currBindingEntity)]]")
                .style("width:100%", "height:100%", "object-fit:contain");
        final DomElement messageElement =  new DomElement("span")
                .style("font-size: 18px", "color: #BDBDBD", "margin: 24px")
                .add(new InnerTextElement("[[_getAltImageText(_currBindingEntity)]]"));
        final DomElement downloadAction = new DomElement("paper-button")
                .style("font-size: 13.3333px", "color: #000000DE")
                .attr("raised", true)
                .attr("on-tap", "_downloadAttachment")
                .attr("tooltip-text", "Downloads the attachment.")
                .add(new DomElement("span").add(new InnerTextElement("DOWNLOAD")));
        final DomElement altImage = new DomElement("div")
                .style("background-color: white")
                .clazz("fit", "layout vertical center-center")
                .attr("hidden$", "[[_isImageVisible(_currBindingEntity)]]")
                .add(messageElement, downloadAction);
        final DomElement container = new DomElement("div")
                .attr("slot", "property-editors")
                .clazz("relative")
                .style("width:100%", "height:100%","padding:8px","box-sizing:border-box")
                .add(altImage, img);

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, "")
                .replace(ENTITY_TYPE, flattenedNameOf(AttachmentPreviewEntityAction.class))
                .replace("<!--@tg-entity-master-content-->", container.toString())
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
                self._isNecessaryForConversion = function (propertyName) {
                    return ['attachment', 'attachmentUri'].indexOf(propertyName) >= 0;
                };
                self._getImageUri = function (entity) {
                    const newEntity = entity ? entity['@@origin'] : null;
                    if (newEntity && newEntity.attachmentUri) {
                        return newEntity.attachmentUri;
                    }
                }.bind(self);
                self._isImageVisible = function (entity) {
                    const newEntity = entity ? entity['@@origin'] : null;
                    if (newEntity && !newEntity.attachmentUri) {
                        return false;
                    }
                    return true;
                }.bind(self);
                self._getAltImageText = function (entity) {
                    const newEntity = entity ? entity['@@origin'] : null;
                    if (newEntity && !newEntity.attachmentUri) {
                        return 'Preview is not available for this file. Please download it instead.';
                    }
                    return '';
                }.bind(self);
                self.downloadAttachment = self.mkDownloadAttachmentFunction();
                self._downloadAttachment = function (e) {
                    if (this._currEntity && this._currEntity.attachment) {
                        this.downloadAttachment(this._currEntity.attachment);
                    }
                }.bind(self);
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
