package ua.com.fielden.platform.sample.domain;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.stream.Stream.of;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Non-persistent enum-like composite entity that is used in CentreUpdaterTest for testing centre diff serialisation; used as property value in {@link TgCentreDiffSerialisation}.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Key")
@CompanionObject(ITgCentreDiffSerialisationNonPersistentCompositeChild.class)
@EntityTitle("Grouping Property")
public class TgCentreDiffSerialisationNonPersistentCompositeChild extends AbstractEntity<DynamicEntityKey> {
    
    @IsProperty
    @Title("Key 1")
    @CompositeKeyMember(1)
    private String key1;
    
    @IsProperty
    @Title("Key 2")
    @CompositeKeyMember(2)
    private String key2;
    
    @Observable
    public TgCentreDiffSerialisationNonPersistentCompositeChild setKey2(final String key2) {
        this.key2 = key2;
        return this;
    }
    
    public String getKey2() {
        return key2;
    }
    
    @Observable
    public TgCentreDiffSerialisationNonPersistentCompositeChild setKey1(final String key1) {
        this.key1 = key1;
        return this;
    }
    
    public String getKey1() {
        return key1;
    }
    
    public enum GroupingProperty {
        TEAM_WEST("Team", "West"),
        TEAM_EAST("Team", "East"),
        PERSON_GOOD("Person", "Good"),
        PERSON_BAD("Person", "Bad");
        
        public final String key1;
        public final String key2;
        public final TgCentreDiffSerialisationNonPersistentCompositeChild value;
        
        GroupingProperty(final String key1, final String key2) {
            this.key1 = key1;
            this.key2 = key2;
            this.value = new TgCentreDiffSerialisationNonPersistentCompositeChild();
            this.value.setKey1(key1);
            this.value.setKey2(key2);
        }
        
        public static GroupingProperty fromValue(final TgCentreDiffSerialisationNonPersistentCompositeChild value) {
            final String key1 = value.getKey1();
            final String key2 = value.getKey2();
            return of(values())
                .filter(gp -> gp.key1.equals(key1) && gp.key2.equals(key2))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Value [%s %s] is not supported.", key1, key2)));
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
                if ((value.key1 + " " + value.key2).equals(key)) {
                    return Optional.of(value);
                }
            }
            return empty();
        }
    }
    
}