package ua.com.fielden.platform.swing.review.report.centre.binder;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.swing.StyledLabel;

public class PropertyBinderEnhancer {

    private static Logger logger = Logger.getLogger(PropertyBinderEnhancer.class);

    private static final String FILTER_BY = "filter by", DOTS = "...";

    /**
     * Enhances existing property editors (specifically labels) by correct titles/description (labels + toolTips) using unified TG algorithm.
     *
     * @param editors
     * @param ed
     */
    public static <T extends AbstractEntity<?>> void enhancePropertyEditors(final Class<T> entityType, final Map<String, IPropertyEditor> editors, final boolean master) {
	final Pair<Class<?>, Set<String>> realRootPropertyNames = getRealRootAndPropertyNames(entityType, editors.keySet());
	final EntityDescriptor ed = new EntityDescriptor(realRootPropertyNames.getKey(), new ArrayList<String>(realRootPropertyNames.getValue()));
	for (final String propertyName : editors.keySet()) {
	    final Pair<Class<?>, String> realRootAndPropertyName = getPropertyRootAndName(entityType, propertyName);
	    final Pair<String, String> tad = ed.getTitleAndDesc(realRootAndPropertyName.getValue());
	    if (tad != null) {
		final IPropertyEditor pe = editors.get(propertyName);

		final JLabel label = pe.getLabel();
		label.setFont(new Font("SansSerif", Font.BOLD, 12));
		label.setForeground(new Color(0x646464)); // 0x858585
		label.setText(tad.getKey() + ":");
		// Important : there are some problems when using setText(stringOfLessLength). Use StyledLabel.clearStyleRanges() to remove internal layout exceptions
		// (BasicStyledLabelUI).
		((StyledLabel) label).clearStyleRanges();
		label.setToolTipText(tad.getValue());

		enhanceEditor(pe, entityType, realRootAndPropertyName, ed, master);
	    } else {
		logger.debug("There is no title and desc retrieved from property [" + propertyName + "] in klass [" + entityType + "] using unified TG algorithm.");
	    }
	}
    }

    /**
     * Returns the pair of entity type and list of properties.
     * If the entityType parameter is an {@link EntityQueryCriteria} class then it returns pair of real entity type and checked property names.
     * 
     * @param entityType
     * @param propertyNames
     * @return
     */
    private static <T extends AbstractEntity<?>> Pair<Class<?>, Set<String>> getRealRootAndPropertyNames(final Class<T> entityType, final Set<String> propertyNames){
	final Set<Class<?>> realEntityTypes = new HashSet<Class<?>>();
	final Set<String> realPropertyNames = new HashSet<String>();
	for(final String propertyName : propertyNames){
	    final Pair<Class<?>, String> realRootAndPropertyName = getPropertyRootAndName(entityType, propertyName);
	    realEntityTypes.add(realRootAndPropertyName.getKey());
	    realPropertyNames.add(realRootAndPropertyName.getValue());
	}
	if(realEntityTypes.size() != 1){
	    throw new IllegalStateException("The property names are associated with more then one entity type!");
	}
	return new Pair<Class<?>, Set<String>>(realEntityTypes.toArray(new Class<?>[0])[0], realPropertyNames);
    }

    /**
     * Returns the real root and property for specified root and property name. (It is needed when the passed root and property name are related to criteria property).
     *
     * @param root
     * @param propertyName
     * @return
     */
    private static <T extends AbstractEntity<?>> Pair<Class<?>, String> getPropertyRootAndName(final Class<T> root, final String propertyName){
	final CriteriaProperty criteriaProperty = AnnotationReflector.getPropertyAnnotation(CriteriaProperty.class, root, propertyName);
	if(criteriaProperty == null){
	    return new Pair<Class<?>, String>(root, propertyName);
	} else {
	    return new Pair<Class<?>, String>(criteriaProperty.rootType(), criteriaProperty.propertyName());
	}
    }

    private static <T extends AbstractEntity<?>> void enhanceEditor(final IPropertyEditor propertyEditor, final Class<T> entityType, final Pair<Class<?>, String> realRootAndPropertyName, final EntityDescriptor ed, final boolean master){
	if (propertyEditor.getEditor() instanceof BoundedValidationLayer) {
	    final BoundedValidationLayer<?> bvl = (BoundedValidationLayer<?>) propertyEditor.getEditor();

	    bvl.setCaption(createCaption(propertyEditor.getPropertyName(), entityType, realRootAndPropertyName, ed, master));
	    bvl.setToolTip(createTooltip(propertyEditor.getPropertyName(), entityType, realRootAndPropertyName, ed, master));
	}
    }

    private static<T extends AbstractEntity<?>> String createCaption(final String propertyName, final Class<T> entityType, final Pair<Class<?>, String> realRootAndPropertyName, final EntityDescriptor ed, final boolean master) {
	final String strWithoutHtml = TitlesDescsGetter.removeHtml(ed.getTitle(realRootAndPropertyName.getValue()));
	return createString(propertyName, entityType, master, strWithoutHtml, "");
    }

    private static <T extends AbstractEntity<?>> String createTooltip(final String propertyName, final Class<T> entityType, final Pair<Class<?>, String> realRootAndPropertyName, final EntityDescriptor ed, final boolean master) {
	final String topDesc = ed.getDescTop(realRootAndPropertyName.getValue());
	final String strWithoutHtml = TitlesDescsGetter.removeHtmlTag(master ? topDesc : topDesc.toLowerCase());
	final String s = createString(propertyName, entityType, master, strWithoutHtml, strWithoutHtml);
	return TitlesDescsGetter.addHtmlTag(s + "<br>" + ed.getDescBottom(realRootAndPropertyName.getValue()));
    }

    private static <T extends AbstractEntity<?>> String createString(final String propertyName, final Class<T> entityType, final boolean master, final String strWithoutHtml, final String masterStr) {
	final String s;
	final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(entityType, propertyName);
	final boolean isFirstParam = CriteriaReflector.isFirstParam(entityType, propertyName);
	final boolean isSecondParam = CriteriaReflector.isSecondParam(entityType, propertyName);
	final boolean isBoolean = boolean.class.isAssignableFrom(propertyType) || Boolean.class.isAssignableFrom(propertyType);
	final boolean isRange = EntityUtils.isRangeType(propertyType);

	if(master){
	    s = masterStr;
	}else if (isRange) {
	    if (isFirstParam) {
		s = FILTER_BY + " " + strWithoutHtml + " from" + DOTS;
	    } else if (isSecondParam) {
		s = FILTER_BY + " " + strWithoutHtml + " to" + DOTS;
	    } else {
		s = FILTER_BY + " " + strWithoutHtml + DOTS;
	    }
	} else if (isBoolean) {
	    if (isFirstParam) {
		s = FILTER_BY + ": is " + strWithoutHtml + DOTS;
	    } else if (isSecondParam) {
		s = FILTER_BY + ": is not " + strWithoutHtml + DOTS;
	    } else {
		s = FILTER_BY + " " + strWithoutHtml + DOTS;
	    }
	} else {
	    s = FILTER_BY + " " + strWithoutHtml + DOTS;
	}
	return s;
    }

    //    /**
    //     * See {@link EntityDescriptor#enhanceDynamicCriteriaPropertyEditorKey(String, Class)} for more details.
    //     *
    //     * @param editors
    //     * @param klass
    //     * @return
    //     */
    //    private static List<String> enhancePropertyNames(final Map<String, IPropertyEditor> editors, final Class klass) {
    //	final List<String> list = new ArrayList<String>();
    //	for (final String s : editors.keySet()) {
    //	    list.add(EntityDescriptor.enhanceDynamicCriteriaPropertyEditorKey(s, klass));
    //	}
    //	return list;
    //    }
}
