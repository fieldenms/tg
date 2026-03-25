package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.tiny.IActionIdentifier;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@KeyType(NoKey.class)
@CompanionObject(Action3Co.class)
public class Action3 extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public static final IActionIdentifier ACTION_ID_ACTION3 = IActionIdentifier.of("Action3");
    public static final String COMPUTED_STRING_VALUE = "Rocket science";

    public enum Properties implements CharSequence {
        selectedIds,
        computedString,
        masterEntity,
        chosenProperty
        ;

        @Override
        public int length() {
            return name().length();
        }

        @Override
        public char charAt(final int index) {
            return name().charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return name().subSequence(start, end);
        }
    }

    protected Action3() {
        setKey(NoKey.NO_KEY);
    }

    @IsProperty(Long.class)
    private final Set<Long> selectedIds = new LinkedHashSet<>();

    @IsProperty
    private String computedString;

    @IsProperty
    private AbstractEntity<?> masterEntity;

    @IsProperty
    private String chosenProperty;

    public String getChosenProperty() {
        return chosenProperty;
    }

    @Observable
    public Action3 setChosenProperty(final String chosenProperty) {
        this.chosenProperty = chosenProperty;
        return this;
    }

    public AbstractEntity<?> getMasterEntity() {
        return masterEntity;
    }

    @Observable
    public Action3 setMasterEntity(final AbstractEntity<?> masterEntity) {
        this.masterEntity = masterEntity;
        return this;
    }

    public String getComputedString() {
        return computedString;
    }

    @Observable
    public Action3 setComputedString(final String computedString) {
        this.computedString = computedString;
        return this;
    }

    @Observable
    protected Action3 setSelectedIds(final Set<Long> selectedIds) {
        this.selectedIds.clear();
        this.selectedIds.addAll(selectedIds);
        return this;
    }

    public Set<Long> getSelectedIds() {
        return unmodifiableSet(selectedIds);
    }

}
