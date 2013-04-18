/*
 * Created by JFormDesigner on Wed Mar 05 17:37:26 EET 2008
 */

package ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.development;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

import sun.swing.SwingUtilities2;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterLogic;
import ua.com.fielden.platform.swing.components.smart.development.Hover;

/**
 * This list cell renderer is designed to represent instances with two specified in the constructor properties: First property is in bold and located above the second property; the
 * second property has a smaller font size.
 *
 * @author 01es
 */
public class MultiplePropertiesListCellRenderer<T> extends JPanel implements ListCellRenderer<T> {
    private static final long serialVersionUID = 1L;

    private final int insets = 5;

    private final Expression[] propertyExpressions;

    private AutocompleterLogic<T> auto;

    private final JLabel[] jlProperties;
    private final Color hoverColour = new Color(201, 227, 251, 150);
    private final Color hoverSelectedColour = new Color(hoverColour.getRed(), hoverColour.getGreen(), hoverColour.getBlue(), 255);

    private final String[] exprProperties;
    private final Map<String, Boolean> exprHighlightMap = new HashMap<>();

    /**
     * Defines the preferred width for pop up component. And it is used for determining the cell renderer text.
     */
    private int preferredWidth = 150;

    public MultiplePropertiesListCellRenderer(final String mainExpression, final String... secondaryExpressions) {
	// create expression for mainExpression
	final int secondaryExpressionLength = secondaryExpressions == null ? 0 : secondaryExpressions.length;
	propertyExpressions = new Expression[1 + secondaryExpressionLength];
	exprProperties = new String[1 + secondaryExpressionLength];
	jlProperties = new JLabel[1 + secondaryExpressionLength];
	try {
	    propertyExpressions[0] = ExpressionFactory.createExpression(("entity." + mainExpression.trim()));
	    jlProperties[0] = createMainLabel();
	    exprProperties[0] = mainExpression;
	    exprHighlightMap.put(mainExpression, Boolean.TRUE);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalArgumentException("Failed to create expression " + mainExpression + ": " + e.getMessage());
	}
	// create expressions for secondaryExpressions
	String rowConstraint = "";
	for (int exprIndex = 0; exprIndex < secondaryExpressionLength; exprIndex++) {
	    try {
		propertyExpressions[exprIndex + 1] = ExpressionFactory.createExpression(("entity." + secondaryExpressions[exprIndex]));
		rowConstraint += "[]";
		jlProperties[exprIndex + 1] = createSecondartLabel();
		exprProperties[exprIndex + 1] = secondaryExpressions[exprIndex];
		exprHighlightMap.put(secondaryExpressions[exprIndex], Boolean.FALSE);
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new IllegalArgumentException("Failed to create expression " + secondaryExpressions[exprIndex] + ": " + e.getMessage());
	    }
	}

	setLayout(new MigLayout("fill, insets " + insets, "[]", "[]" + rowConstraint + insets)); // there will be a gap after each entry in the list
	for (int compIndex = 0; compIndex < jlProperties.length - 1; compIndex++) {
	    add(jlProperties[compIndex], "grow, wrap");
	}
	add(jlProperties[jlProperties.length - 1], "grow");

    }

    private JLabel createMainLabel() {
	final JLabel jlName = new JLabel();
	jlName.setText("text");
	jlName.setFont(jlName.getFont().deriveFont(jlName.getFont().getStyle() | Font.BOLD));
	jlName.setBorder(new EmptyBorder(0, insets, 0, 0));
	jlName.setPreferredSize(new Dimension(preferredWidth, 17));
	return jlName;
    }

    private JLabel createSecondartLabel() {
	final JLabel jlDesc = new JLabel();
	jlDesc.setText("text");
	jlDesc.setBorder(new EmptyBorder(0, insets, 0, 0));
	jlDesc.setFont(new Font("DejaVu Sans", Font.PLAIN, 10));
	jlDesc.setPreferredSize(new Dimension(preferredWidth, 13));
	return jlDesc;
    }

    @SuppressWarnings("unchecked")
    private String value(final Object entity, final int index) {
	final JexlContext jc = JexlHelper.createContext();
	jc.getVars().put("entity", entity);
	try {
	    return propertyExpressions[index].evaluate(jc) + "";
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException("Failed to evaluate expression " + propertyExpressions[index] + ": " + e.getMessage());
	}
    }

    public Component getListCellRendererComponent(final JList<? extends T> list, final T value, final int index, final boolean isSelected, final boolean cellHasFocus) {
	if (isSelected) {
	    if (Hover.index(list) == index) {
		setBackground(hoverSelectedColour);
	    } else {
		setBackground(list.getSelectionBackground());
	    }
	    setForeground(list.getSelectionForeground());
	} else {
	    if (Hover.index(list) == index) {
		setBackground(hoverColour);
	    } else {
		setBackground(list.getBackground());
	    }
	    setForeground(list.getForeground());
	}
	//////////////////////////////////////////////////////////////////////////////
	//// set values to be rendered with highlighting of the matched portion //////
	//////////////////////////////////////////////////////////////////////////////
	// * if string starts with % then remove it
	// * if string ends with % then also remove it
	// * if string does not end with % then append $
	// * substitute all occurrences of % with .*
	final String fullPattern = auto.getPrevValue().toUpperCase();
	final String fullPatternS1 = removeFromStart(fullPattern, "%");
	final String fullPatternS2 = fullPatternS1.endsWith("%") ? removeFromEnd(fullPatternS1, "%") : fullPatternS1 + "$";
	final String strPattern = fullPatternS2.replaceAll("\\%", ".*");
	final Pattern pattern = Pattern.compile(strPattern);

	for (int exprIndex = 0; exprIndex < exprProperties.length; exprIndex++) {
	    final String exprValue = value(value, exprIndex);
	    final JLabel label = jlProperties[exprIndex];
	    final FontMetrics fm = SwingUtilities2.getFontMetrics(label, label.getFont());
	    final String clippedString = SwingUtilities2.clipStringIfNecessary(label, fm, exprValue, preferredWidth);
	    label.setText(isPropertyHighlighted(exprProperties[exprIndex]) ? matchValue(exprValue, clippedString, pattern) : TitlesDescsGetter.addHtmlTag(clippedString));
	}
	return this;
    }

    //    @Override
    //    @Transient
    //    public Dimension getPreferredSize() {
    //        final Dimension superDim = super.getPreferredSize();
    //        final int textCompWidth = auto.getTextComponent().getWidth();
    //        final BasicLabelUI lableUi = new BasicLabelUI();
    //        final int mainLabelWidth = lableUi.getPreferredSize(jlProperties[0]).width;
    //        int maxSecLabelWidth = 0;
    //        for(int lbIndex = 0; lbIndex < jlProperties.length; lbIndex++){
    //            final int labelWidth = lableUi.getPreferredSize(jlProperties[lbIndex]).width;
    //            if (maxSecLabelWidth < labelWidth) {
    //        	maxSecLabelWidth = labelWidth;
    //            }
    //        }
    //        System.out.println(superDim);
    //        return superDim;
    //        if (mainLabelWidth > textCompWidth) {
    //            return new Dimension(mainLabelWidth, superDim.height);
    //        } else if (maxSecLabelWidth > textCompWidth) {
    //            return new Dimension(textCompWidth, superDim.height);
    //        } else {
    //            return superDim;
    //        }
    //    }

    /**
     * Determines whether expression property must be selected or not.
     *
     * @param exprProperty
     * @return
     */
    public boolean isPropertyHighlighted(final String exprProperty) {
	if (exprHighlightMap.containsKey(exprProperty)) {
	    return exprHighlightMap.get(exprProperty);
	}
	throw new IllegalArgumentException("The expression " + exprProperty + " is not included in to this cell renderer");
    }

    /**
     * Set the expression property value that determines whether it must be selected or not.
     *
     * @param exprProperty
     * @param highlight
     */
    public void setPropertyToHighlight(final String exprProperty, final boolean highlight) {
	if (exprHighlightMap.containsKey(exprProperty)) {
	    exprHighlightMap.put(exprProperty, highlight);
	    return;
	}
	throw new IllegalArgumentException("The expression " + exprProperty + " is not included in to this cell renderer");
    }

    /**
     * Returns highlighted matched value.
     *
     * @param value
     *            - value to highlight.
     * @param clippedString
     * @param pattern
     *            - {@link Pattern} to which value value should be matched.
     * @return
     */
    private String matchValue(final String value, final String clippedString, final Pattern pattern) {
	String  suffix = "";
	String body = clippedString;
	if(clippedString.endsWith("...")){
	    suffix = "...";
	    body = clippedString.substring(0, clippedString.length() - 3);
	}
	final String fullNameUpper = value.toUpperCase();
	final Matcher matcher = pattern.matcher(fullNameUpper);
	final StringBuffer buffer = new StringBuffer();
	buffer.append("<html>");
	if (matcher.find() && matcher.start() < body.length()) {
	    buffer.append(body.substring(0, matcher.start()));
	    buffer.append("<font bgcolor=#fffec7>");
	    final int endIndex = matcher.end() <= body.length() ? matcher.end() : body.length();
	    buffer.append(body.substring(matcher.start(), endIndex));
	    buffer.append("</font>");
	    buffer.append(body.substring(endIndex));
	    buffer.append(suffix);
	} else {
	    buffer.append(clippedString);
	}
	buffer.append("</html>");
	return buffer.toString();
    }

    /**
     * A convenient method to remove leading wild cards.
     *
     * @param value
     * @param whatToRemove
     * @return
     */
    private String removeFromStart(final String value, final String whatToRemove) {
	final String result = value.startsWith("%") ? value.substring(1) : value;
	return value.startsWith("%") ? removeFromStart(result, whatToRemove) : result;
    }

    /**
     * A convenient method to remove trailing wild cards.
     *
     * @param value
     * @param whatToRemove
     * @return
     */
    private String removeFromEnd(final String value, final String whatToRemove) {
	final String result = value.endsWith("%") ? value.substring(0, value.length() - 1) : value;
	return value.endsWith("%") ? removeFromEnd(result, whatToRemove) : result;
    }

    public AutocompleterLogic<T> getAuto() {
	return auto;
    }

    public void setAuto(final AutocompleterLogic<T> auto) {
	this.auto = auto;
    }

    /**
     * Set the preferred width for cell renderer.
     *
     * @param preferredWidth
     */
    public void setPreferredWidth(final int preferredWidth){
	this.preferredWidth = preferredWidth - insets * 3;
    }

//    /**
//     * Updates the preferred width of the renderer according to the new list model.
//     *
//     * @param entities
//     */
//    public void updatePreferredWidth(final List<T> entities) {
//	int maxWidth = 150;
//	for(final T entity : entities) {
//	    final int nextElemWidth = calculateElementWidth(entity);
//	    if(maxWidth < nextElemWidth) {
//		maxWidth = nextElemWidth;
//	    }
//	}
//	if (entities.isEmpty()) {
//	    System.out.println("list size updated -- " + preferredWidth);
//	    this.preferredWidth = maxWidth;
//	}
//    }
//
//    /**
//     * Calculates the preferred width for entity.
//     *
//     * @param entity
//     * @return
//     */
//    private int calculateElementWidth(final T entity) {
//	final int textCompWidth = auto.getTextComponent().getWidth() - insets * 3;
//	final BasicLabelUI lableUi = new BasicLabelUI();
//	jlProperties[0].setText(value(entity, 0));
//	final int mainLabelWidth = lableUi.getPreferredSize(jlProperties[0]).width - insets;
//	int maxSecLabelWidth = 0;
//	for (int lbIndex = 1; lbIndex < jlProperties.length; lbIndex++) {
//	    jlProperties[lbIndex].setText(value(entity, lbIndex));
//	    final int labelWidth = lableUi.getPreferredSize(jlProperties[lbIndex]).width - insets;
//	    if (maxSecLabelWidth < labelWidth) {
//		maxSecLabelWidth = labelWidth;
//	    }
//	}
//	if (mainLabelWidth > textCompWidth) {
//	    return mainLabelWidth;
//	} else if (maxSecLabelWidth > textCompWidth) {
//	    return textCompWidth;
//	} else {
//	    return -1;
//	}
//    }

}
