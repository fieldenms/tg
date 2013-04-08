package ua.com.fielden.platform.migration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Updater;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;
import static ua.com.fielden.platform.migration.RetrieverPropsValidator.RetrievedPropValidationError.INAPPROPRIATE_BUT_PRESENT;
import static ua.com.fielden.platform.migration.RetrieverPropsValidator.RetrievedPropValidationError.INCORRECTLY_SPELLED;
import static ua.com.fielden.platform.migration.RetrieverPropsValidator.RetrievedPropValidationError.REQUIRED_BUT_MISSING;

final class RetrieverPropsValidator {
    private final DomainMetadataAnalyser dma;
    private final Class<? extends AbstractEntity<?>> entityType;
    private final Set<String> retrievedProps;
    private final boolean updater;

    public RetrieverPropsValidator(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> ret) {
	this.dma = dma;
	this.entityType = ret.type();
	this.retrievedProps = ret.resultFields().keySet();
	this.updater = AnnotationReflector.isAnnotationPresent(Updater.class, ret.getClass());
    }

    public enum RetrievedPropValidationError {
	INCORRECTLY_SPELLED, INAPPROPRIATE_BUT_PRESENT, REQUIRED_BUT_MISSING
    }

    SortedMap<String, RetrievedPropValidationError> validate() {
	final SortedMap<String, RetrievedPropValidationError> result = new TreeMap<String, RetrievedPropValidationError>();

	final Set<String> incorrectlySpelledProps = getIncorrectlySpelledProps();
	result.putAll(markWithError(incorrectlySpelledProps, INCORRECTLY_SPELLED));

	final Set<String> correctlySpelledProps = subtract(retrievedProps, incorrectlySpelledProps);
	final Set<String> requiredProps = updater ? Collections.<String>emptySet() : subtract(dma.getEntityMetadata(entityType).getNotNullableProps(), new HashSet<String>(){{add("id"); add("version");}});
	final Set<String> expectedProps = getExpectedSubprops(union(requiredProps, union(EntityUtils.getFirstLevelProps(correctlySpelledProps), Finder.getFieldNames(Finder.getKeyMembers(entityType)))));

	result.putAll(markWithError(subtract(correctlySpelledProps, expectedProps), INAPPROPRIATE_BUT_PRESENT));
	result.putAll(markWithError(subtract(expectedProps, correctlySpelledProps), REQUIRED_BUT_MISSING));
	return result;
    }

    private Set<String> getIncorrectlySpelledProps() {
	final Set<String> result = new HashSet<String>();
	for (final String prop : retrievedProps) {
	    if (dma.getInfoForDotNotatedProp(entityType, prop) == null) {
		result.add(prop);
	    }
	}
	return result;
    }

    private Map<String, RetrievedPropValidationError> markWithError(final Set<String> props, final RetrievedPropValidationError error) {
	final Map<String, RetrievedPropValidationError> result = new HashMap<String, RetrievedPropValidationError>();
	for (final String string : props) {
	    result.put(string, error);
	}
	return result;
    }


    private Set<String> getExpectedSubprops(final Set<String> firstLevelProps) {
	return dma.getLeafPropsFromFirstLevelProps(null, entityType, firstLevelProps);
    }

    private static Set<String> subtract(final Collection<String> set1, final Collection<String> set2) {
	return new HashSet<String>(CollectionUtils.subtract(set1, set2));
    }

    private static Set<String> union(final Collection<String> set1, final Collection<String> set2) {
	return new HashSet<String>(CollectionUtils.union(set1, set2));
    }
}