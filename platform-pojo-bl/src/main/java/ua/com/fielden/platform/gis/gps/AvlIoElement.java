package ua.com.fielden.platform.gis.gps;

public class AvlIoElement {

    public static final int BASE_ELEMENT_LENGTH = 1 + 1 + 1 + 1 + 1 + 1;

    public static int convertId(final byte id) {
	return id >= 0 ? id : 256 + id;
    }

    public static class ByteIoElement {

	public static final int BASE_ELEMENT_LENGTH = 2;

	public final int ioId;
	public final byte ioValue;

	public ByteIoElement(final byte id, final byte value) {
	    this.ioId = convertId(id);
	    this.ioValue = value;
	}

	@Override
	public String toString() {
	    return "id = " + ioId + "\tvalue = " + ioValue;
	}
    };

    public static class ShortIoElement {

	public static final int BASE_ELEMENT_LENGTH = 3;

	public final int ioId;
	public final short ioValue;

	public ShortIoElement(final byte id, final short value) {
	    this.ioId = convertId(id);
	    this.ioValue = value;
	}

	@Override
	public String toString() {
	    return "id = " + ioId + "\tvalue = " + ioValue;
	}
    };

    public static class IntIoElement {

	public static final int BASE_ELEMENT_LENGTH = 5;

	public final int ioId;
	public final int ioValue;

	public IntIoElement(final byte id, final int value) {
	    this.ioId = convertId(id);
	    this.ioValue = value;
	}

	@Override
	public String toString() {
	    return "id = " + ioId + "\tvalue = " + ioValue;
	}
    };

    public static class LongIoElement {

	public static final int BASE_ELEMENT_LENGTH = 9;

	public final int ioId;
	public final long ioValue;

	public LongIoElement(final byte id, final long value) {
	    this.ioId = convertId(id);
	    this.ioValue = value;
	}

	@Override
	public String toString() {
	    return "id = " + ioId + "\tvalue = " + ioValue;
	}
    };

    public final byte eventId;
    public final byte ioTotal;
    public final ByteIoElement[] byteIo;
    public final ShortIoElement[] shortIo;
    public final IntIoElement[] intIo;
    public final LongIoElement[] longIo;

    public AvlIoElement(final byte eventId, final byte ioTotal, final ByteIoElement[] byteIo, final ShortIoElement[] shortIo, final IntIoElement[] intIo, final LongIoElement[] longIo) {
	this.eventId = eventId;
	this.ioTotal = ioTotal;
	this.byteIo = byteIo;
	this.shortIo = shortIo;
	this.intIo = intIo;
	this.longIo = longIo;
    }

}
