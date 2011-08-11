package ua.com.fielden.platform.serialisation.impl.serialisers.not_used;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.tokens.conditions.Condition;
import ua.com.fielden.platform.equery.tokens.conditions.GroupCondition;
import ua.com.fielden.platform.equery.tokens.main.ConditionsGroup;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

import com.esotericsoftware.kryo.SerializationException;

/**
 * Serialises {@link GroupCondition} instances.
 * 
 * @author TG Team
 * 
 */
public class GroupConditionSerialiser extends TgSimpleSerializer<GroupCondition> {

    private final Field logicalOperatorField;
    private final Field groupField;
    private final Field negatedField;

    public GroupConditionSerialiser(final TgKryo kryo) {
	super(kryo);
	try {
	    logicalOperatorField = Condition.class.getDeclaredField("logicalOperator");
	    logicalOperatorField.setAccessible(true);
	    groupField = GroupCondition.class.getDeclaredField("group");
	    groupField.setAccessible(true);
	    negatedField = GroupCondition.class.getDeclaredField("negated");
	    negatedField.setAccessible(true);
	} catch (final Exception e) {
	    throw new SerializationException("Could not obtain fields for GroupCondition type.");
	}
    }

    @Override
    public void write(final ByteBuffer buffer, final GroupCondition value) {
	try {
	    writeValue(buffer, logicalOperatorField.get(value));
	    writeValue(buffer, groupField.get(value));
	    writeBoolean(buffer, (Boolean) negatedField.get(value));
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not write GroupCondition: " + e.getMessage());
	}
    }

    @Override
    public GroupCondition read(final ByteBuffer buffer) {
	try {
	    final GroupCondition gc = new GroupCondition();
	    logicalOperatorField.set(gc, readValue(buffer, LogicalOperator.class));
	    groupField.set(gc, readValue(buffer, ConditionsGroup.class));
	    negatedField.set(gc, readBoolean(buffer));
	    return gc;
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not read GroupCondition: " + e.getMessage());
	}
    }

}
