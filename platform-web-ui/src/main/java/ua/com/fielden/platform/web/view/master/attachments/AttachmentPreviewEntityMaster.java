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
                .attr("id", "imageLoader")
                .attr("src$", "[[_getImageUri(_currBindingEntity)]]")
                .attr("hidden$", "[[!_isImageVisible(_loadingError, _currBindingEntity)]]")
                .style("width:100%", "height:100%", "object-fit:contain");
        final DomElement messageElement =  new DomElement("span")
                .style("font-size: 18px", "color: #BDBDBD", "margin: 24px")
                .add(new InnerTextElement("[[_getAltImageText(_currBindingEntity)]]"));
        final DomElement downloadAction = new DomElement("paper-button")
                .style("font-size: 14px", "font-weight: 500", "color: #000000DE")
                .attr("raised", true)
                .attr("on-tap", "_downloadOrOpenAttachment")
                .attr("tooltip-text", "[[_getButtonTooltip(_currBindingEntity)]]")
                .add(new DomElement("span").add(new InnerTextElement("[[_getButtonName(_currBindingEntity)]]")));
        final DomElement altImage = new DomElement("div")
                .style("background-color: white")
                .clazz("fit", "layout vertical center-center")
                .attr("hidden$", "[[_isImageVisible(_loadingError, _currBindingEntity)]]")
                .add(messageElement, downloadAction);
        final DomElement container = new DomElement("div")
                .attr("slot", "property-editors")
                .clazz("relative")
                .style("width:100%", "height:100%","padding:8px","box-sizing:border-box")
                .add(altImage, img);

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, "import { isSupportedLink, checkLinkAndOpen, canOpenLinkWithoutConfirmation } from '/resources/components/tg-link-opener.js'")
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
                self.$.imageLoader.addEventListener('load', () => {
                    self._loadingError = false;
                });
                self.$.imageLoader.addEventListener('error', () => {
                    self._loadingError = true;
                });
                self._isNecessaryForConversion = function (propertyName) {
                    return ['attachment', 'attachmentUri'].indexOf(propertyName) >= 0;
                };
                self._handleBindingEntityChanged = function (e) {
                    if (e.detail.value) {
                        isSupportedLink
                    }
                };
                self.addEventListener('_curr-binding-entity-changed', self._handleBindingEntityChanged.bind(self));
                self._getImageUri = function (entity) {
                    const newEntity = entity ? entity['@@origin'] : null;
                    if (newEntity && newEntity.attachmentUri) {
                        if (isSupportedLink(newEntity.attachmentUri)) {
                            const checkLinkRes = canOpenLinkWithoutConfirmation(newEntity.attachmentUri);
                            if (checkLinkRes && !checkLinkRes.canOpenWithoutConfirmation) {
                                return "";
                            }
                        }
                        return newEntity.attachmentUri;    
                    }
                }.bind(self);
                self._getButtonName = function (_currBindingEntity) {
                    const newEntity = entity ? entity['@@origin'] : null;
                    if (newEntity && newEntity.attachmentUri) {
                        if (isSupportedLink(newEntity.attachmentUri)) {
                            return "OPEN";
                        }
                    }
                    return "DOWNLOAD";
                };
                self._getButtonTooltip = function (_currBindingEntity) {
                    const newEntity = entity ? entity['@@origin'] : null;
                    if (newEntity && newEntity.attachmentUri) {
                        if (isSupportedLink(newEntity.attachmentUri)) {
                            return "Opens the attachment.";
                        }
                    }
                    return "Downloads the attachment.";
                };
                self._isImageVisible = function (loadingError, entity) {
                    if (loadingError) {
                        return false;
                    }
                    const newEntity = entity ? entity['@@origin'] : null;
                    if (newEntity && !newEntity.attachmentUri) {
                        return false;
                    }
                    return true;
                }.bind(self);
                self._getAltImageText = function (entity) {
                    const newEntity = entity ? entity['@@origin'] : null;
                    if (newEntity && !newEntity.attachmentUri) {
                        if (isSupportedLink(newEntity.attachmentUri)) {
                            const checkLinkRes = canOpenLinkWithoutConfirmation(newEntity.attachmentUri);
                            if (checkLinkRes && !checkLinkRes.canOpenWithoutConfirmation) {
                                return "This preview isn’t from a trusted source. Please confirm that you trust it by clicking the OPEN button below.";
                            }
                            return 'Preview is not available for this link attachment. Please open it instead.';
                        }
                        return 'Preview is not available for this file. Please download it instead.';
                    }
                    return '';
                }.bind(self);
                self.downloadAttachment = self.mkDownloadAttachmentFunction();
                self._downloadOrOpenAttachment = function (e) {
                    if (this._currEntity && this._currEntity.attachment) {
                        if (isSupportedLink(this._currEntity.attachmentUri)) {
                            checkLinkAndOpen(this._currEntity.attachmentUri);
                        } else {
                            this.downloadAttachment(this._currEntity.attachment);
                        }
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
