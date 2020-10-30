package ua.com.fielden.platform.reflection;

import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationOptionally;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.firstAndRest;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isDotNotation;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.titles.Subtitles;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.Pair;

/**
 * This is a helper class to provide methods related to property/entity titles/descs determination.
 *
 * @author TG Team
 *
 */
public class TitlesDescsGetter {
    public static final char LEFT_ARROW = '\u2190';
    public static final Pair<String, String> EMPTY_TITLE_AND_DESC = pair("", "");
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
        final List<String> pathOfTitles = new ArrayList<>();
        final List<String> pathOfDescs = new ArrayList<>();
        for (final String propertyName : properties) {
            pathOfTitles.add(getTitleAndDesc(propertyName, ownerType).getKey());
            pathOfDescs.add(getTitleAndDesc(propertyName, ownerType).getValue());
            ownerType = PropertyTypeDeterminator.determineClass(ownerType, propertyName, true, true);
        }

        return new Pair<>(pathOfTitles, pathOfDescs);
    }

    /**
     * Returns full property title and description (usually EGI columns titles and toolTips) in form of "reversed path of titles" (e.g. "Status code<-Status<-Vehicle") and
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
        return pair(path, "<html><i><b>" + removeHtmlTag(list.getValue().get(list.getValue().size() - 1)) + "</b></i><br><i>[" + path + "]</i></html>");
    }

    public static String addHtmlTag(final String str) {
        return "<html>" + str + "</html>";
    }

    public static String removeHtmlTag(final String str) {
        return StringUtils.remove(StringUtils.remove(str, "<html>"), "</html>");
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
     * Returns a {@link Pair} with {@code key} set to property title (taken either from {@link Title}, or {@link KeyTitle}, or {@link DescTitle}, or {@link Subtitles}) and {@code value} set to property description.
     *
     * @param propPath -- a property name or a dot-notated property path.
     * @param entityType -- a type that holds the first property in {@code propPath}
     * @return
     */
    public static Pair<String, String> getTitleAndDesc(final String propPath, final Class<?> entityType) {
            return processSubtitles(propPath, entityType).orElseGet(() -> processTitles(propPath, entityType));
    }

    /**
     * Determines property titles and desc without analysing {@link Subtitles}. Effectively this represents the logic before subtitles were introduced.
     * This method should not be used directly and therefore it is private.
     * 
     * @param propPath
     * @param entityType
     * @return
     */
    private static Pair<String, String> processTitles(final String propPath, final Class<?> entityType) {
        final boolean containsKey = KEY.equals(propPath) || propPath.endsWith("." + KEY);
        final boolean containsDesc = DESC.equals(propPath) || propPath.endsWith("." + DESC);
        
        if (!containsKey && !containsDesc) {
            return getPropertyAnnotationOptionally(Title.class, entityType, propPath)
                       .map(annotation -> pair(annotation.value(), annotation.desc().isEmpty() ? annotation.value() : annotation.desc()))
                       .orElseGet(() -> getTitleAndDescOfPropertyType(propPath, entityType).map(p -> pair(p.getKey(), p.getKey())).orElse(EMPTY_TITLE_AND_DESC));
        } 
        
        if (containsKey) {
            return getPropertyAnnotationOptionally(KeyTitle.class, entityType, propPath)
                   .map(annotation -> pair(annotation.value(), annotation.desc().isEmpty() ? annotation.value() : annotation.desc()))
                   .orElse(EMPTY_TITLE_AND_DESC);
        }
        
        return getPropertyAnnotationOptionally(DescTitle.class, entityType, propPath)
                    .map(annotation -> pair(annotation.value(), annotation.desc().isEmpty() ? annotation.value() : annotation.desc()))
                    .orElse(EMPTY_TITLE_AND_DESC);
        
    }

    /**
     * Determines property title and desc from {@link Subtitles} if applicable. Returns an empty optional otherwise. 
     *  
     * @param propPath
     * @param entityType
     * @return
     */
    private static Optional<Pair<String, String>> processSubtitles(final String propPath, final Class<?> entityType) {
        if (isDotNotation(propPath)) {
            final String propName = firstAndRest(propPath).getKey();
            final Optional<Subtitles> subtitles = getPropertyAnnotationOptionally(Subtitles.class, entityType, propName);
            return subtitles.flatMap(sub -> Stream.of(sub.value()).filter(pt -> (propName + "." + pt.path()).equals(propPath)).findFirst().map(pt -> Pair.pair(pt.title(), pt.desc()))); 
        } else {
            return Optional.empty();
        }
    }

    /**
     * If <code>dotNotationExp</code> refers to an entity-typed property of <code>propOwnerType</code> then a pair of title and description of of the entity-type is returned.
     * Otherwise, an empty result is returned. 
     * 
     * @param dotNotationExp
     * @param propOwnerType
     * @return
     */
    public static Optional<Pair<String, String>> getTitleAndDescOfPropertyType(final String dotNotationExp, final Class<?> propOwnerType) {
        final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(propOwnerType, dotNotationExp);
        if (AbstractEntity.class.isAssignableFrom(propertyType)) {
            final Class<? extends AbstractEntity<?>> type = (Class<? extends AbstractEntity<?>>) propertyType;
            return Optional.of(getEntityTitleAndDesc(type));
        }
        
        return Optional.empty();
    }

    /**
     * Returns {@link TitlesDescsGetter#getEntityTitleAndDesc(Class)} title/desc, modified to show "collectional" relationship with <code>entityWithCollectionalPropertyType</code>.
     *
     * @return
     */
    public static Pair<String, String> getEntityTitleAndDescInCollectionalPropertyContex(final Class<? extends AbstractEntity<?>> collectionalPropertyType, final Class<?> entityWithCollectionalPropertyType) {
        final Pair<String, String> tad = getEntityTitleAndDesc(collectionalPropertyType);

        // TODO : improve!
        return new Pair<>(tad.getKey() + "-es", tad.getValue() + "-es");
    }

    /**
     * Returns {@link Pair} with key set to entity title and value set to entity description. Traverses <code>entityType</code> hierarchy bottom-up in search of the specified
     * entity title and description.
     *
     * @param entityType
     * @return
     */
    public static Pair<String, String> getEntityTitleAndDesc(final Class<? extends AbstractEntity<?>> entityType) {
        return AnnotationReflector.getAnnotationOptionally(entityType, EntityTitle.class)
               .map(annotation -> pair(annotation.value(), annotation.desc()))
               .orElseGet(() -> getDefaultEntityTitleAndDesc(entityType));
    }

    /**
     * Provides default values of title and description for entity. (e.g. "VehicleFinDetails.class" => "Vehicle Fin Details" and "Vehicle Fin Details entity")
     */
    public static Pair<String, String> getDefaultEntityTitleAndDesc(final Class<? extends AbstractEntity<?>> klass) {
        final String s = breakClassName(klass.getSimpleName());
        return new Pair<>(s, s + " entity");
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

    public static String processReqErrorMsg(final String propName, final Class<? extends AbstractEntity<?>> entityType) {
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

    public static String processEntityExistsErrorMsg(final String propName, final Object errouneousValue, final Class<? extends AbstractEntity<?>> entityType) {
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
        } catch (final ReflectionException e) {
            e.printStackTrace();
        }

        return errorMsg;
    }

}
