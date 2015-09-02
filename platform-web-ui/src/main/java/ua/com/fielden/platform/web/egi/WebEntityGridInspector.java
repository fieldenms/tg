package ua.com.fielden.platform.web.egi;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.interfaces.IRenderable;

public class WebEntityGridInspector implements IRenderable {

    private final EntityCentre<? extends AbstractEntity<?>> entityCentre;

    private final String simpleValueString = "<div class='data-entry layout vertical' property='@calc-property-name'>" +
            "<div class='data-label truncate'>@column-title</div>" +
            "<div class='data-value relative' on-tap='_tapAction'>" +
            "<div style$='[[_calcRenderingHintsStyle(egiEntity, entityIndex, \"@property-name\")]]' class='fit'></div>" +
            "<div class='truncate relative'>[[_getValue(egiEntity.entity, '@property-name', '@property-type')]]</div>" +
            "</div>" +
            "</div>";

    private final String booleanValueString = "<div class='data-entry layout vertical' property='@calc-property-name'>" +
            "<div class='data-label truncate'>@column-title</div>" +
            "<div class='data-value relative' on-tap='_tapAction'>" +
            "<div style$='[[_calcRenderingHintsStyle(egiEntity, entityIndex, \"@property-name\")]]' class='fit'></div>" +
            "<iron-icon class='card-icon' icon='[[_getBooleanIcon(egiEntity.entity, \"@property-name\")]]'></iron-icon>" +
            "</div>" +
            "</div>";

    public WebEntityGridInspector(final EntityCentre<? extends AbstractEntity<?>> entityCentre) {
        this.entityCentre = entityCentre;
    }

    @Override
    public DomElement render() {
        final DomContainer domContainer = new DomContainer();
        final Optional<List<ResultSetProp>> resultProps = entityCentre.getCustomPropertiesDefinitions();
        final Class<?> managedType = entityCentre.getAssociatedCentre().getEnhancer().getManagedType(entityCentre.getEntityType());
        if (resultProps.isPresent()) {
            for (final ResultSetProp resultProp : resultProps.get()) {
                final String propertyName = resultProp.propDef.isPresent() ? CalculatedProperty.generateNameFrom(resultProp.propDef.get().title) : resultProp.propName.get();
                final String resultPropName = propertyName.equals("this") ? "" : propertyName;
                final Class<?> propertyType = "".equals(resultPropName) ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, resultPropName);
                final String typeTemplate = EntityUtils.isBoolean(propertyType) ? booleanValueString : simpleValueString;
                domContainer.add(
                        new InnerTextElement(typeTemplate
                                .replaceAll("@calc-property-name", propertyName)
                                .replaceAll("@property-name", resultPropName)
                                .replaceAll("@column-title", CriteriaReflector.getCriteriaTitleAndDesc(managedType, resultPropName).getKey())
                                .replaceAll("@property-type", Matcher.quoteReplacement(egiRepresentationFor(propertyType).toString()))));
            }
        }
        final String text = ResourceLoader.getText("ua/com/fielden/platform/web/egi/tg-entity-grid-inspector-template.html");
        final String egiStr = text.
                replaceAll("@miType", entityCentre.getMenuItemType().getSimpleName()).
                replaceAll("@gridCardDom", Matcher.quoteReplacement(domContainer.toString()));
        return new InnerTextElement(egiStr);
    }

    private Object egiRepresentationFor(final Class<?> propertyType) {
        return EntityUtils.isEntityType(propertyType) ? propertyType.getName() : (EntityUtils.isBoolean(propertyType) ? "Boolean" : propertyType.getSimpleName());
    }
}
