package ua.com.fielden.platform.reflection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.utils.Pair;

/**
 * This is a helper class to provide methods related to property/entity titles/descs determination.
 *
 * @author TG Team
 *
 */
public class TitlesDescsGetter {
    public static final char LEFT_ARROW = '\u2190';

    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private TitlesDescsGetter() {
    }

    /**
     *
     * Returns property path from <code>type</code> to specified property that consists of property titles and descriptions.
     *
     * @param type
     * @param dotNotationExp
     * @return
     */
    private static Pair<List<String>, List<String>> getPropertyTitlesAndDescriptionsPath(final Class<?> type, final String dotNotationExp) {
	final String[] properties = dotNotationExp.split(Reflector.DOT_SPLITTER);
	Class<?> ownerType = type;
	final List<String> pathOfTitles = new ArrayList<String>();
	final List<String> pathOfDescs = new ArrayList<String>();
	for (final String propertyName : properties) {
	    pathOfTitles.add(getTitleAndDesc(propertyName, ownerType).getKey());
	    pathOfDescs.add(getTitleAndDesc(propertyName, ownerType).getValue());
	    ownerType = PropertyTypeDeterminator.determineClass(ownerType, propertyName, true, true);
	}

	return new Pair<List<String>, List<String>>(pathOfTitles, pathOfDescs);
    }

    /**
     * Returns full property title and description (usually Egi columns titles and toolTips) in form of "reversed path of titles" (e.g. "Status code<-Status<-Vehicle") and
     * "property description" (e.g. "[Vehicle Status code]").
     *
     * @param propertyName
     * @param parentKlass
     *
     * @return
     */
    public static Pair<String, String> getFullTitleAndDesc(final String propertyName, final Class<?> parentKlass) {
	final String path = pathOfTitles(parentKlass, propertyName);
	final Pair<List<String>, List<String>> list = getPropertyTitlesAndDescriptionsPath(parentKlass, propertyName);
	return new Pair<String, String>(path, "<html><i><b>" + removeHtmlTag(list.getValue().get(list.getValue().size() - 1)) + "</b></i><br><i>[" + path + "]</i></html>");
    }

    /**
     * Remove all html tags from string.
     *
     * @param str
     * @return
     */
    public static String removeHtml(final String str) {
        return str.replaceAll("\\<.*?\\>", "");
    }

    public static String addHtmlTag(final String str) {
	return "<html>" + str + "</html>";
    }

    public static String removeHtmlTag(final String str) {
	return str.replaceAll("<html>", "").replaceAll("</html>", "");
    }

    public static String italic(final String s) {
	return addHtmlTag("<i>" + s + "</i>");
    }

    public static String removeItalic(final String s) {
	return removeHtmlTag(s.replaceAll("<i>", "").replaceAll("</i>", ""));
    }

    private static String pathOfTitles(final Class<?> parentKlass, final String propertyName) {
	final List<String> reversedTitledPath = TitlesDescsGetter.getPropertyTitlesAndDescriptionsPath(parentKlass, propertyName).getKey();
	Collections.reverse(reversedTitledPath);
	final Iterator<String> iterator = reversedTitledPath.iterator();
	final StringBuilder builder = new StringBuilder();
	if (iterator.hasNext()) {
	    builder.append(iterator.next());
	} else {
	    return null;
	}
	while (iterator.hasNext()) {
	    builder.append(LEFT_ARROW + iterator.next());
	}
	return builder.toString();
    }

    /**
     * Returns {@link Pair} with key set to property title (taken either from {@link Title}, or {@link KeyTitle}, or {@link DescTitle}) and value set to property description
     *
     * @param propertyName
     * @param entityType
     * @return
     */
    public static Pair<String, String> getTitleAndDesc(final String propertyName, final Class<?> entityType) {
	final KeyTitle keyTitleAnnotation = AnnotationReflector.getPropertyAnnotation(KeyTitle.class, entityType, propertyName);
	final DescTitle descTitleAnnotation = AnnotationReflector.getPropertyAnnotation(DescTitle.class, entityType, propertyName);
	final Title titleAnnotation = AnnotationReflector.getPropertyAnnotation(Title.class, entityType, propertyName);

	final boolean containsKey = AbstractEntity.KEY.equals(propertyName) || propertyName.endsWith("." + AbstractEntity.KEY);
	final boolean containsDesc = AbstractEntity.DESC.equals(propertyName) || propertyName.endsWith("." + AbstractEntity.DESC);
	final String title = containsKey ? (keyTitleAnnotation != null ? keyTitleAnnotation.value() : "") //
		: containsDesc ? (descTitleAnnotation != null ? descTitleAnnotation.value() : "") //
			: titleAnnotation != null ? titleAnnotation.value() : "";
	// If desc() is not specified in corresponding annotation then use value() instead:
	final String desc = containsKey ? (keyTitleAnnotation != null ? (keyTitleAnnotation.desc().isEmpty() ? keyTitleAnnotation.value() : keyTitleAnnotation.desc()) : "") //
		: containsDesc ? (descTitleAnnotation != null ? (descTitleAnnotation.desc().isEmpty() ? descTitleAnnotation.value() : descTitleAnnotation.desc()) : "") //
			: titleAnnotation != null ? (titleAnnotation.desc().isEmpty() ? titleAnnotation.value() : titleAnnotation.desc()) : "";

	return new Pair<String, String>(title, desc);
    }

    /**
     * Returns {@link TitlesDescsGetter#getEntityTitleAndDesc(Class)} title/desc, modified to show "collectional" relationship with <code>entityWithCollectionalPropertyType</code>.
     *
     * @return
     */
    public static Pair<String, String> getEntityTitleAndDescInCollectionalPropertyContex(final Class<?> collectionalPropertyType, final Class<?> entityWithCollectionalPropertyType) {
	final Pair<String, String> tad = getEntityTitleAndDesc(collectionalPropertyType);

	// TODO : improve!
	return new Pair<String, String>(tad.getKey() + "-es", tad.getValue() + "-es");
    }

    /**
     * Returns {@link Pair} with key set to entity title and value set to entity description. Traverses <code>entityType</code> hierarchy bottom-up in search of the specified
     * entity title and description.
     *
     * @param entityType
     * @return
     */
    public static Pair<String, String> getEntityTitleAndDesc(final Class<?> entityType) {
	final EntityTitle entityTitleAnnotation = AnnotationReflector.getAnnotation(entityType, EntityTitle.class);
	final String title = entityTitleAnnotation != null ? entityTitleAnnotation.value() : "";
	final String desc = entityTitleAnnotation != null ? entityTitleAnnotation.desc() : "";

	final Pair<String, String> detad = getDefaultEntityTitleAndDesc(entityType);
	return new Pair<String, String>(title.isEmpty() ? detad.getKey() : title, desc.isEmpty() ? detad.getValue() : desc);
    }

    /**
     * Provides default values of title and description for entity. (e.g. "VehicleFinDetails.class" => "Vehicle Fin Details" and "Vehicle Fin Details entity")
     */
    private static Pair<String, String> getDefaultEntityTitleAndDesc(final Class<?> klass) {
	final String s = breakClassName(klass.getSimpleName());
	return new Pair<String, String>(s, s + " entity");
    }

    private static String breakClassName(final String str) {
	String temp = str;
	int i = 0;
	while (firstUpperCaseLetterIndex(temp) == 0) { // iterate to find first lowerCase letter
	    temp = str.substring(++i);
	}
	final String upperCasePart = str.substring(0, i);
	final int firstUpperCaseLetterIndex = firstUpperCaseLetterIndex(temp);
	return upperCasePart
		+ (firstUpperCaseLetterIndex < 0 ? temp : temp.substring(0, firstUpperCaseLetterIndex) + " " + breakClassName(temp.substring(firstUpperCaseLetterIndex)));
    }

    private static int firstUpperCaseLetterIndex(final String str) {
	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z') {
		return i;
	    }
	}
	return -1;
    }


    public static String processReqErrorMsg(final String propName, final Class<?> entityType) {
	String errorMsg = "";
	if (AbstractEntity.KEY.equals(propName)) {
	    if (AnnotationReflector.isAnnotationPresentForClass(KeyTitle.class, entityType)) {
		errorMsg = AnnotationReflector.getAnnotation(entityType, KeyTitle.class).reqErrorMsg();
	    } else {
		errorMsg = "";
	    }
	} else if (AbstractEntity.DESC.equals(propName)) {
	    if (AnnotationReflector.isAnnotationPresentForClass(DescRequired.class, entityType)) {
		errorMsg = AnnotationReflector.getAnnotation(entityType, DescRequired.class).value();
	    } else {
		errorMsg = "";
	    }
	} else {
	    final Required anRequired = AnnotationReflector.getPropertyAnnotation(Required.class, entityType, propName);
	    errorMsg = anRequired != null ? anRequired.value() : "";
	}
	// template processing
	if (!StringUtils.isEmpty(errorMsg)) {

	    final String propTitle = TitlesDescsGetter.getTitleAndDesc(propName, entityType).getKey();
	    errorMsg = errorMsg.replace("{{prop-title}}", StringUtils.isEmpty(propTitle) ? propName : propTitle);
	    errorMsg = errorMsg.replace("{{entity-title}}", TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey());
	}
	return errorMsg;
    }

    public static String processEntityExistsErrorMsg(final String propName, final Object errouneousValue, final Class<?> entityType) {
	String errorMsg = "";

	try {
	    final Method setter = Reflector.obtainPropertySetter(entityType, propName);
	    if (AnnotationReflector.isAnnotationPresent(setter, EntityExists.class)) {
		errorMsg = AnnotationReflector.getAnnotation(setter, EntityExists.class).errorMsg();
		final String propTitle = TitlesDescsGetter.getTitleAndDesc(propName, entityType).getKey();
		errorMsg = errorMsg.replace("{{prop-title}}", StringUtils.isEmpty(propTitle) ? propName : propTitle);
		errorMsg = errorMsg.replace("{{prop-value}}", errouneousValue + "");
		errorMsg = errorMsg.replace("{{entity-title}}", TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey());
	    }
	} catch (final NoSuchMethodException e) {
	    e.printStackTrace();
	}

	return errorMsg;
    }

}
