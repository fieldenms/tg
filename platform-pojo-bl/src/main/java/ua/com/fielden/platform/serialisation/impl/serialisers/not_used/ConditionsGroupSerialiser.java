package ua.com.fielden.platform.serialisation.impl.serialisers.not_used;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.tokens.conditions.ComparisonOperation;
import ua.com.fielden.platform.equery.tokens.conditions.GroupCondition;
import ua.com.fielden.platform.equery.tokens.main.ConditionsGroup;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

import com.esotericsoftware.kryo.SerializationException;

/**
 * Serialises {@link ConditionsGroup} instances.
 * 
 * @author TG Team
 * 
 */
public class ConditionsGroupSerialiser extends TgSimpleSerializer<ConditionsGroup> {

    private final Field currGroupConditionField;
    private final Field currPropertyField;
    private final Field currLogicalOperatorField;
    private final Field currOperationField;
    private final Field currNegatedField;
    private final Field conditionsField;
    private final Field parentGroupReferenceField;
    private final Field isCurrentField;
    private final Field onConditionField;

    public ConditionsGroupSerialiser(final TgKryo kryo) {
	super(kryo);
	try {
	    currGroupConditionField = ConditionsGroup.class.getDeclaredField("currGroupCondition");
	    currGroupConditionField.setAccessible(true);

	    currPropertyField = ConditionsGroup.class.getDeclaredField("currProperty");
	    currPropertyField.setAccessible(true);

	    currLogicalOperatorField = ConditionsGroup.class.getDeclaredField("currLogicalOperator");
	    currLogicalOperatorField.setAccessible(true);

	    currOperationField = ConditionsGroup.class.getDeclaredField("currOperation");
	    currOperationField.setAccessible(true);

	    currNegatedField = ConditionsGroup.class.getDeclaredField("currNegated");
	    currNegatedField.setAccessible(true);

	    conditionsField = ConditionsGroup.class.getDeclaredField("conditions");
	    conditionsField.setAccessible(true);

	    parentGroupReferenceField = ConditionsGroup.class.getDeclaredField("parentGroupReference");
	    parentGroupReferenceField.setAccessible(true);

	    isCurrentField = ConditionsGroup.class.getDeclaredField("isCurrent");
	    isCurrentField.setAccessible(true);

	    onConditionField = ConditionsGroup.class.getDeclaredField("onCondition");
	    onConditionField.setAccessible(true);

	} catch (final Exception e) {
	    throw new SerializationException("Could not obtain fields for ConditionsGroup type.");
	}
    }

    @Override
    public void write(final ByteBuffer buffer, final ConditionsGroup qm) {
	try {
	    writeValue(buffer, currGroupConditionField.get(qm));
	    writeValue(buffer, currPropertyField.get(qm));
	    writeValue(buffer, currLogicalOperatorField.get(qm));
	    writeValue(buffer, currOperationField.get(qm));
	    writeValue(buffer, conditionsField.get(qm));
	    writeValue(buffer, parentGroupReferenceField.get(qm));

	    writeValue(buffer, isCurrentField.get(qm));
	    writeValue(buffer, onConditionField.get(qm));
	    writeValue(buffer, currNegatedField.get(qm));

	    writeBoolean(buffer, (Boolean) isCurrentField.get(qm));
	    writeBoolean(buffer, (Boolean) onConditionField.get(qm));
	    writeBoolean(buffer, (Boolean) currNegatedField.get(qm));
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not write ConditionsGroup: " + e.getMessage());
	}
    }

    @Override
    public ConditionsGroup read(final ByteBuffer buffer) {
	try {
	    final ConditionsGroup cg = new ConditionsGroup();
	    currGroupConditionField.set(cg, readValue(buffer, GroupCondition.class));
	    currPropertyField.set(cg, readValue(buffer, SearchProperty.class));
	    currLogicalOperatorField.set(cg, readValue(buffer, LogicalOperator.class));
	    currOperationField.set(cg, readValue(buffer, ComparisonOperation.class));
	    conditionsField.set(cg, readValue(buffer, ArrayList.class));
	    parentGroupReferenceField.set(cg, readValue(buffer, ConditionsGroup.class));

	    isCurrentField.set(cg, readBoolean(buffer));
	    onConditionField.set(cg, readBoolean(buffer));
	    currNegatedField.set(cg, readBoolean(buffer));
	    return cg;
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not read ConditionsGroup: " + e.getMessage());
	}
    }

}
