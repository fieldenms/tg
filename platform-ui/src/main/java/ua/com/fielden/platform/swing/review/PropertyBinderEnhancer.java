package ua.com.fielden.platform.swing.review;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.EntityDescriptor;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
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
    public static void enhancePropertyEditors(final Class klass, final Map<String, IPropertyEditor> editors, final boolean master) {
	final EntityDescriptor ed = new EntityDescriptor(klass, EntityDescriptor.getPropertyNamesWithoutKeyParts(enhancePropertyNames(editors, klass)));
	for (final String propertyName : editors.keySet()) {
	    final String appropriatePropertyName = EntityDescriptor.getPropertyNameWithoutKeyPart(EntityDescriptor.enhanceDynamicCriteriaPropertyEditorKey(propertyName, klass));
	    final Pair<String, String> tad = ed.getTitleAndDesc(appropriatePropertyName);
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

		enhanceEditor(pe, klass, appropriatePropertyName, ed, master);
	    } else {
		logger.debug("There is no title and desc retrieved from property [" + propertyName + "] in klass [" + klass + "] using unified TG algorithm.");
	    }
	}
    }

    public static void enhanceEditor(final IPropertyEditor propertyEditor, final Class klass, final String appropriatePropertyName, final EntityDescriptor ed, final boolean master){
	if (propertyEditor.getEditor() instanceof BoundedValidationLayer) {
	    final BoundedValidationLayer bvl = (BoundedValidationLayer) propertyEditor.getEditor();

	    bvl.setCaption(createCaption(propertyEditor.getPropertyName(), klass, appropriatePropertyName, ed, master));
	    bvl.setToolTip(createTooltip(propertyEditor.getPropertyName(), klass, appropriatePropertyName, ed, master));
	}
    }

    public static final String FILTER_BY = "filter by", DOTS = "...", CHANGE = "change";

    private static String createCaption(final String propertyName, final Class klass, final String appropriatePropertyName, final EntityDescriptor ed, final boolean master) {
	final String strWithoutHtml = TitlesDescsGetter.removeHtml(ed.getTitle(appropriatePropertyName));
	return createString(propertyName, klass, appropriatePropertyName, master, strWithoutHtml, "");
    }

    private static String createTooltip(final String propertyName, final Class klass, final String appropriatePropertyName, final EntityDescriptor ed, final boolean master) {
	final String topDesc = ed.getDescTop(appropriatePropertyName);
	final String strWithoutHtml = TitlesDescsGetter.removeHtmlTag(master ? topDesc : topDesc.toLowerCase());
	final String s = createString(propertyName, klass, appropriatePropertyName, master, strWithoutHtml, strWithoutHtml);
	return TitlesDescsGetter.addHtmlTag(s + "<br>" + ed.getDescBottom(appropriatePropertyName));
    }

    private static String createString(final String propertyName, final Class klass, final String appropriatePropertyName, final boolean master, final String strWithoutHtml, final String masterStr) {
	final String s;
	if (propertyName.endsWith(DynamicEntityQueryCriteria._FROM)){
	    // is "range" editor single?
	    final CritOnly critOnly = AnnotationReflector.getPropertyAnnotation(CritOnly.class, klass, appropriatePropertyName);
	    final boolean single = critOnly != null && Type.SINGLE.equals(critOnly.value());

	    s = FILTER_BY + " " + strWithoutHtml + (single ? "" : (" " + "from")) + DOTS;
	} else if (propertyName.endsWith(DynamicEntityQueryCriteria._TO)){
	    s = FILTER_BY + " " + strWithoutHtml + " " + "to" + DOTS;
	} else if (propertyName.endsWith(DynamicEntityQueryCriteria._IS)){
	    s = FILTER_BY + ": is " + strWithoutHtml + DOTS;
	} else if (propertyName.endsWith(DynamicEntityQueryCriteria._NOT)){
	    s = FILTER_BY + ": is not " + strWithoutHtml + DOTS;
	} else {
	    s = master ? masterStr : (FILTER_BY + " " + strWithoutHtml + DOTS);
	}
	return s;
    }

    /**
     * See {@link EntityDescriptor#enhanceDynamicCriteriaPropertyEditorKey(String, Class)} for more details.
     *
     * @param editors
     * @param klass
     * @return
     */
    private static List<String> enhancePropertyNames(final Map<String, IPropertyEditor> editors, final Class klass) {
	final List<String> list = new ArrayList<String>();
	for (final String s : editors.keySet()) {
	    list.add(EntityDescriptor.enhanceDynamicCriteriaPropertyEditorKey(s, klass));
	}
	return list;
    }

}
