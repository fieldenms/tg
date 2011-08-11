package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.nio.ByteBuffer;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import ua.com.fielden.platform.serialisation.impl.TgKryo;

import com.esotericsoftware.kryo.serialize.IntSerializer;

public class SortKeySerialiser extends TgSimpleSerializer<SortKey> {

    public SortKeySerialiser(final TgKryo kryo) {
	super(kryo);
    }

    @Override
    public SortKey read(final ByteBuffer buffer) {
	final int column = IntSerializer.get(buffer, true);
	final SortOrder sortOrder = readValue(buffer, SortOrder.class);
	return new SortKey(column, sortOrder);
    }

    @Override
    public void write(final ByteBuffer buffer, final SortKey sortKey) {
	IntSerializer.put(buffer, sortKey.getColumn(), true);
	writeValue(buffer, sortKey.getSortOrder());
    }

}
