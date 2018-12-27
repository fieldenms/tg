package ua.com.fielden.platform.sample.domain;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.stream.Stream.of;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Non-persistent enum-like entity that is used in CentreUpdaterTest for testing centre diff serialisation; used as property value in {@link TgCentreDiffSerialisation}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@CompanionObject(ITgCentreDiffSerialisationNonPersistentChild.class)
@EntityTitle("Grouping Property")
public class TgCentreDiffSerialisationNonPersistentChild extends AbstractEntity<String> {
    
    public enum GroupingProperty {
        TEAM("Team"),
        PERSON("Person");
        
        public final String key;
        public final TgCentreDiffSerialisationNonPersistentChild value;
        
        GroupingProperty(final String key) {
            this.key = key;
            this.value = new TgCentreDiffSerialisationNonPersistentChild();
            this.value.setKey(key);
        }
        
        public static GroupingProperty fromValue(final TgCentreDiffSerialisationNonPersistentChild value) {
            final String key = value.getKey();
            return of(values())
                .filter(gp -> gp.key.equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Value [%s] is not supported.", key)));
        }
        
        /**
         * Finds grouping property by its unique key.
         *
         * @param key
         * @return
         */
        public static Optional<GroupingProperty> findByKey(final String key) {
            final GroupingProperty[] allValues = values();
            for (final GroupingProperty value : allValues) {
                if (value.key.equals(key)) {
                    return Optional.of(value);
                }
            }
            return empty();
        }
    }
    
}