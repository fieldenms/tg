package ua.com.fielden.platform.reflection;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.titles.Subtitles;
import ua.com.fielden.platform.utils.Pair;

import java.util.*;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.*;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationOptionally;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.*;
import static ua.com.fielden.platform.utils.EntityUtils.isCriteriaEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.laxSplitPropPathToArray;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalPropertyName;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalType;

/// This is a helper class to provide methods related to property/entity titles/descs determination.
///
public class TitlesDescsGetter {
    public static final char LEFT_ARROW = '\u2190';
    public static final Pair<String, String> EMPTY_TITLE_AND_DESC = pair("", "");

    /// Returns property path from `type` to specified property that consists of property titles and descriptions.
    ///
    private static Pair<List<String>, List<String>> getPropertyTitlesAndDescriptionsPath(final Class<?> type, final String dotNotationExp) {
        final String[] properties = laxSplitPropPathToArray(dotNotationExp);
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

    /// Returns the full property title and description (usually EGI columns titles and toolTips),
    /// represented as a "reversed path of titles" (e.g. "Status code<-Status<-Vehicle") and
    /// "property description" (e.g. "Vehicle Status Code").
    ///
    public static Pair<String, String> getFullTitleAndDesc(final String propertyName, final Class<?> parentKlass) {
        final String path = pathOfTitles(parentKlass, propertyName);
        final Pair<List<String>, List<String>> list = getPropertyTitlesAndDescriptionsPath(parentKlass, propertyName);
        return pair(path, "<html><i><b>" + removeHtmlTag(list.getValue().getLast()) + "</b></i><br><i>[" + path + "]</i></html>");
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

    /// Returns a [Pair] whose `key` is the property title and whose `value` is the property description.
    ///
    /// The title and description are resolved from one of the following, in order of precedence:
    /// [Title], [KeyTitle], [DescTitle], or [Subtitles].
    ///
    /// @param propPath   the property name or dot-notated property path
    /// @param entityType the type that declares the first property in `propPath`
    /// @return a pair containing the resolved title (key) and description (value)
    ///
    public static Pair<String, String> getTitleAndDesc(final CharSequence propPath, final Class<?> entityType) {
            return processSubtitles(propPath.toString(), entityType).orElseGet(() -> processTitles(propPath.toString(), entityType));
    }

    /// Determines property titles and desc without analysing [Subtitles].
    /// Effectively this represents the logic before subtitles were introduced.
    /// This method should not be used directly and therefore it is private.
    ///
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

    /// Determines property title and desc from [Subtitles] if applicable.
    /// Returns an empty optional otherwise.
    ///
    private static Optional<Pair<String, String>> processSubtitles(final String propPath, final Class<?> entityType) {
        if (isDotExpression(propPath)) {
            final String propName = firstAndRest(propPath).getKey();
            final Optional<Subtitles> subtitles = getPropertyAnnotationOptionally(Subtitles.class, entityType, propName);
            return subtitles.flatMap(sub -> Stream.of(sub.value()).filter(pt -> (propName + "." + pt.path()).equals(propPath)).findFirst().map(pt -> Pair.pair(pt.title(), pt.desc())));
        } else {
            return Optional.empty();
        }
    }

    /// Returns the title and description of a property based on the property’s type.
    ///
    /// If the property is entity-typed, the title and description of that entity type are used.
    /// Otherwise, both title and description are derived from the property name by converting it
    /// into a readable, space-separated form.
    ///
    /// @param dotNotationExp the property name or dot-notated property path
    /// @param propOwnerType  the root type from which the property path is resolved
    /// @return an optional pair containing the resolved title (key) and description (value)
    ///
    public static Optional<Pair<String, String>> getTitleAndDescOfPropertyType(final String dotNotationExp, final Class<?> propOwnerType) {
        final Class<?> propertyType = determinePropertyType(propOwnerType, dotNotationExp);
        if (AbstractEntity.class.isAssignableFrom(propertyType)) {
            final Class<? extends AbstractEntity<?>> type = (Class<? extends AbstractEntity<?>>) propertyType;
            return of(getEntityTitleAndDesc(type));
        }
        final String propName = isDotExpression(dotNotationExp) ? penultAndLast(dotNotationExp).getValue() : dotNotationExp;
        final String readablePropertyName = join(asList(splitByCharacterTypeCamelCase(capitalize(propName))), " ");
        return of(pair(readablePropertyName, readablePropertyName));
    }

    /// Returns the title and description of a property derived from its type.
    ///
    /// If the property is entity-typed, this method uses that entity type’s title and description.
    /// Otherwise, both title and description are derived from the property’s name.
    ///
    /// @param propertyPath the property name or dot-notated property path
    /// @param type         the root type from which the property path is resolved
    /// @return a pair containing the resolved title (key) and description (value)
    ///
    public static Pair<String, String> titleAndDescOfPropertyType(final CharSequence propertyPath, final Class<?> type) {
        final Class<?> propertyType = determinePropertyType(type, propertyPath);
        if (AbstractEntity.class.isAssignableFrom(propertyType)) {
            return getEntityTitleAndDesc((Class<? extends AbstractEntity<?>>) propertyType);
        }
        final var propName = isDotExpression(propertyPath) ? penultAndLast(propertyPath).getValue() : propertyPath;
        final var readablePropName = titleFromPropertyName(propName);
        return pair(readablePropName, readablePropName);
    }

    /// Derives a human-readable title from a property name.
    ///
    /// The property name is capitalized and split on camel-case boundaries,
    /// then the parts are joined with spaces.
    /// For example, `vehicleStatusCode` becomes `"Vehicle Status Code"`.
    ///
    /// @param property the raw property name
    /// @return a human-readable title derived from the property name
    ///
    public static String titleFromPropertyName(final CharSequence property) {
        return String.join(" ", splitByCharacterTypeCamelCase(capitalize(property.toString())));
    }

    /// Retrieves the title of a property, guaranteeing a non-blank result.
    ///
    /// The title is resolved in three steps:
    /// 1. Use the title from [#getTitleAndDesc].
    /// 2. If blank, fall back to [#titleAndDescOfPropertyType].
    /// 3. If still blank, derive a title from the property name via [#titleFromPropertyName].
    ///
    /// @param propPath   the property name or dot-notated property path
    /// @param entityType the type that declares the first property in `propPath`
    /// @return a non-blank title for the specified property
    ///
    public static String nonBlankPropertyTitle(final CharSequence propPath, final Class<?> entityType) {
        var title = getTitleAndDesc(propPath, entityType).getKey();
        if (title.isBlank()) {
            title = titleAndDescOfPropertyType(propPath, entityType).getKey();
        }
        if (title.isBlank()) {
            title = titleFromPropertyName(propPath);
        }
        return title;
    }

    /// Returns the title and description of the specified entity type.
    ///
    /// The most specific [EntityTitle] annotation found in the entity’s hierarchy is used.
    /// If no such annotation is present, the entity type’s name is used to derive a default title
    /// and description.
    ///
    /// @param entityType the entity type whose title and description should be resolved
    /// @return a pair containing the entity title (key) and description (value)
    ///
    public static Pair<String, String> getEntityTitleAndDesc(final Class<? extends AbstractEntity<?>> entityType) {
        return AnnotationReflector.getAnnotationOptionally(entityType, EntityTitle.class)
               .map(annotation -> pair(annotation.value(), annotation.desc()))
               .orElseGet(() -> getDefaultEntityTitleAndDesc(entityType));
    }

    /// Returns the title of the specified entity type.
    ///
    /// The most specific [EntityTitle] annotation found in the entity’s hierarchy is used.
    /// If no such annotation is present, the entity type’s name is used to derive a default title.
    ///
    /// @param entityType the entity type whose title should be resolved
    /// @return the entity title
    ///
    public static String getEntityTitle(final Class<? extends AbstractEntity<?>> entityType) {
        return AnnotationReflector.getAnnotationOptionally(entityType, EntityTitle.class)
                .map(EntityTitle::value)
                .orElseGet(() -> getDefaultEntityTitle(entityType));
    }

    /// Returns a [Pair] whose key is the entity title and whose value is the entity description.
    ///
    /// The title and description are resolved for the runtime type of the given entity by
    /// traversing its type hierarchy bottom‑up in search of an applicable [EntityTitle] definition.
    ///
    /// @param entity the entity instance whose type title and description should be resolved
    /// @return a pair containing the entity title (key) and description (value)
    ///
    public static Pair<String, String> getEntityTitleAndDesc(final AbstractEntity<?> entity) {
        return getEntityTitleAndDesc(entity.getType());
    }

    /// Provides a default title and description for the specified entity type, derived from its class name.
    ///
    /// For example, `VehicleFinDetails.class` yields the pair `("Vehicle Fin Details", "Vehicle Fin Details entity")`.
    ///
    /// @param type the entity type
    /// @return a pair containing the default entity title (key) and description (value)
    ///
    public static Pair<String, String> getDefaultEntityTitleAndDesc(final Class<? extends AbstractEntity<?>> type) {
        final String s = getDefaultEntityTitle(type);
        return pair(s, s + " entity");
    }

    /// Provides a default title for the specified entity type, derived from its simple class name.
    ///
    /// For example, `VehicleFinDetails.class` yields `"Vehicle Fin Details"`.
    ///
    /// @param type the entity type
    /// @return the default entity title
    ///
    public static String getDefaultEntityTitle(final Class<? extends AbstractEntity<?>> type) {
        return breakClassName(type.getSimpleName());
    }

    /// Breaks a simple class name into space-separated words at camel-case boundaries.
    ///
    /// For example, `"MyClassName"` becomes `"My Class Name"`.
    ///
    /// @param classSimpleName the simple (unqualified) class name
    /// @return a human-readable version of the name with spaces inserted between words
    ///
    public static String breakClassName(final String classSimpleName) {
        if (StringUtils.isEmpty(classSimpleName)) {
            return "";
        }
        return Stream.of(classSimpleName.split("(?=\\p{Upper})")).map(String::trim).collect(joining(" "));
    }

    /// Resolves the required-field error message for a given property and entity type.
    ///
    /// For criteria entities, the error message is resolved against the corresponding original entity type and property.
    /// For regular entities, the message is derived from:
    /// * [KeyTitle] for the `key` property (if present);
    /// * [DescRequired] for the `desc` property (if present);
    /// * [Required] for all other properties (if present).
    ///
    /// If a template message is found, the placeholders `{{prop-title}}` and
    /// `{{entity-title}}` are replaced with the resolved property title and entity title
    /// respectively.
    ///
    /// @param propName   the name of the property (or its criteria counterpart)
    /// @param entityType the entity type declaring the property
    /// @return the resolved error message, or an empty string if none is defined
    /// 
    public static String processReqErrorMsg(final String propName, final Class<? extends AbstractEntity<?>> entityType) {
        if (isCriteriaEntityType(entityType)) {
            return processReqErrorMsg(getOriginalPropertyName(entityType, propName), getOriginalType(entityType));
        }
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

    /// Let's hide default constructor, which is not needed for a static class.
    ///
    private TitlesDescsGetter() { }

}
