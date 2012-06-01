package ua.com.fielden.platform.swing.review.report.centre.binder;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.swing.StyledLabel;

public class PropertyBinderEnhancer {

    private static Logger logger = Logger.getLogger(PropertyBinderEnhancer.class);

    /**
     * Enhances existing property editors (specifically labels) by correct titles/description (labels + toolTips) using unified TG algorithm.
     *
     * @param editors
     * @param ed
     */
    public static <T extends AbstractEntity> void enhancePropertyEditors(final Class<T> entityType, final Map<String, IPropertyEditor> editors, final boolean master) {
	final EntityDescriptor ed = new EntityDescriptor(entityType, new ArrayList<String>(editors.keySet()));
	for (final String propertyName : editors.keySet()) {
	    final Pair<String, String> tad = ed.getTitleAndDesc(propertyName);
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

		enhanceEditor(pe, entityType, ed, master);
	    } else {
		logger.debug("There is no title and desc retrieved from property [" + propertyName + "] in klass [" + entityType + "] using unified TG algorithm.");
	    }
	}
    }

    private static <T extends AbstractEntity> void enhanceEditor(final IPropertyEditor propertyEditor, final Class<T> entityType, final EntityDescriptor ed, final boolean master){
	if (propertyEditor.getEditor() instanceof BoundedValidationLayer) {
	    final BoundedValidationLayer bvl = (BoundedValidationLayer) propertyEditor.getEditor();

	    bvl.setCaption(createCaption(propertyEditor.getPropertyName(), entityType, ed, master));
	    bvl.setToolTip(createTooltip(propertyEditor.getPropertyName(), entityType, ed, master));
	}
    }

    private static final String FILTER_BY = "filter by", DOTS = "...";

    private static <T extends AbstractEntity> String createCaption(final String propertyName, final Class<T> entityType, final EntityDescriptor ed, final boolean master) {
	final String strWithoutHtml = TitlesDescsGetter.removeHtml(ed.getTitle(propertyName));
	return createString(propertyName, entityType, master, strWithoutHtml, "");
    }

    private static <T extends AbstractEntity> String createTooltip(final String propertyName, final Class<T> entityType, final EntityDescriptor ed, final boolean master) {
	final String topDesc = ed.getDescTop(propertyName);
	final String strWithoutHtml = TitlesDescsGetter.removeHtmlTag(master ? topDesc : topDesc.toLowerCase());
	final String s = createString(propertyName, entityType, master, strWithoutHtml, strWithoutHtml);
	return TitlesDescsGetter.addHtmlTag(s + "<br>" + ed.getDescBottom(propertyName));
    }

    private static <T extends AbstractEntity> String createString(final String propertyName, final Class<T> entityType, final boolean master, final String strWithoutHtml, final String masterStr) {
	final String s;
	final Class<?> propertyType = PropertyTypeDeterminator.determineClass(entityType, propertyName, true, true);
	final boolean isFirstParam = CriteriaReflector.isFirstParam(entityType, propertyName);
	final boolean isSecondParam = CriteriaReflector.isSecondParam(entityType, propertyName);
	final boolean isBoolean = boolean.class.isAssignableFrom(propertyType) || Boolean.class.isAssignableFrom(propertyType);
	final boolean isRange = EntityUtils.isRangeType(propertyType);

	if(master){
	    s = masterStr;
	}else if (isRange) {
	    if (isFirstParam) {
		s = FILTER_BY + " " + strWithoutHtml + " " + "from" + DOTS;
	    } else if (isSecondParam) {
		s = FILTER_BY + " " + strWithoutHtml + " " + "to" + DOTS;
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
