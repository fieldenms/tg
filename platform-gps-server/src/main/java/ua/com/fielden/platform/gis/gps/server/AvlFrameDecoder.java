package ua.com.fielden.platform.gis.gps.server;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.AvlGpsElement;
import ua.com.fielden.platform.gis.gps.AvlIoElement;

public class AvlFrameDecoder extends FrameDecoder {

    public enum DecodingState {
        CONNECTING, DATA, DISCONNECTED
    };

    private final Logger log = Logger.getLogger(AvlFrameDecoder.class);
    private final Crc16 checksum = new Crc16(0xA001);
    private DecodingState state = DecodingState.CONNECTING;

    @Override
    protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final ChannelBuffer buffer) throws Exception {

        if (buffer.readable() == false) {
            return null;
        }

        final int availableData = buffer.readableBytes();

        switch (state) {

        case CONNECTING:
            if (availableData < 2) {
                return new Exception("Not enough bytes for id size");
            }
            final short imeiLength = buffer.readShort();
            if (availableData < 2 + imeiLength) {
                throw new Exception("Not enough bytes for id content");
            }
            // Read IMEI
            final String imei = new String(buffer.readBytes(imeiLength).array());
            checkpoint(DecodingState.DATA);
            return imei;

        case DATA:
            log.debug("available data count [" + availableData + "]");
            if (availableData < 4 + 4) {
                log.debug("Not enoght header data");
                return null;
            }
            buffer.markReaderIndex();

            final int zero = buffer.readInt();
            final int len = buffer.readInt();

            if (availableData < 4 + 4 + 4 + len) {
                log.debug("Not enoght packet data");
                buffer.resetReaderIndex();
                return null;
            }

            if (zero != 0x0) {
                // report error and disconnect
                throw new Exception("AVL packet ZERO tocken mismatch");
            }

            final int idx = buffer.readerIndex();

            final int crc = buffer.getInt(idx + len);
            checksum.reset();
            for (int i = idx; i < idx + len; i++) {
                checksum.update(buffer.getByte(i));
            }
            final int calculatedCrc = checksum.getCrc();
            // Check CRC16
            if (crc != calculatedCrc) {
                // report error and disconnect
                throw new Exception("AVL packet CRC mismatch: expected [" + crc + "], calculated [" + calculatedCrc + "]");
            }

            // Read AVL
            final byte codec = buffer.readByte();
            switch (codec) {
            case 0x8:
                // FM4100 & FM2100 codec
                log.debug("FM4100 & FM2100 codec data");
                final byte headCount = buffer.readByte();
                final AvlData[] avlArray = new AvlData[headCount];
                int capacity = len - 2;
                for (int i = 0; i < headCount; i++) {
                    final AvlData element = readAvlData(buffer, capacity);
                    capacity -= element.getCapacity();
                    avlArray[i] = element;
                }
                final byte tailCount = buffer.readByte();
                if (headCount != tailCount) {
                    // report error and disconnect
                    throw new Exception("AVL packet data count unequal: head [" + headCount + "], tail [" + tailCount + "]");
                }
                if (capacity != 1) {
                    throw new Exception("Bad packet length: to many data [" + len + "]");
                }
                // skip CRC
                buffer.skipBytes(4);
                log.debug("AVL data count [" + avlArray.length + "]");
                return avlArray;
            default:
                // report unsupported codec and skip data
                log.info("skip codec = [" + codec + "]");
                final int skipped = -1 + len + 4;
                buffer.skipBytes(skipped);
                log.debug("skipped bytes count [" + skipped + "]");
                return false;
            }
        case DISCONNECTED:
            // report state
            log.info("Disconnected!");
            break;
        }

        return null;
    }

    private AvlData readAvlData(final ChannelBuffer buffer, final Integer capacity) throws Exception {
        if (capacity < AvlData.BASE_ELEMENT_LENGTH) {
            throw new Exception("Bad packet length: data overrun provided length");
        }
        // Test for availability of data for IO element
        final int position = buffer.readerIndex();
        int offset = AvlData.BASE_ELEMENT_LENGTH - AvlIoElement.BASE_ELEMENT_LENGTH + 1;

        final byte ioTotal = buffer.getByte(position + offset);
        offset++;
        // Test for 1 byte IO
        final byte byteTotal = buffer.getByte(position + offset);
        offset += 1 + byteTotal * AvlIoElement.ByteIoElement.BASE_ELEMENT_LENGTH;
        // Test for 2 byte IO
        final byte shortTotal = buffer.getByte(position + offset);
        offset += 1 + shortTotal * AvlIoElement.ShortIoElement.BASE_ELEMENT_LENGTH;
        // Test for 4 byte IO
        final byte intTotal = buffer.getByte(position + offset);
        offset += 1 + intTotal * AvlIoElement.IntIoElement.BASE_ELEMENT_LENGTH;
        // Test for 8 byte IO
        final byte longTotal = buffer.getByte(position + offset);
        offset += 1 + longTotal * AvlIoElement.LongIoElement.BASE_ELEMENT_LENGTH;
        // Test IO element count validity
        if (byteTotal + shortTotal + intTotal + longTotal != ioTotal) {
            throw new Exception("Total IO count is incorrect: overall count [" + ioTotal + "], byte count [" + byteTotal + "], short count = [" + shortTotal + "], int count = ["
                    + intTotal + "], long count = [" + longTotal + "]");
        }
        if (capacity < offset) {
            throw new Exception("Bad packet length: data overrun provided length");
        }
        //capacity -= offset;

        // Return AVL data
        final long timestamp = buffer.readLong();
        final byte priority = buffer.readByte();

        final int longitude = buffer.readInt();
        final int latitude = buffer.readInt();
        final short altitude = buffer.readShort();
        final short angle = buffer.readShort();
        final byte satellites = buffer.readByte();
        final short speed = buffer.readShort();
        final AvlGpsElement gps = new AvlGpsElement(longitude, latitude, altitude, angle, satellites, speed);

        final byte eventId = buffer.readByte();
        // Skip ioTotal
        buffer.skipBytes(1);
        // Skip byteTotal
        buffer.skipBytes(1);
        // Iterate and read all bytes IO data
        final AvlIoElement.ByteIoElement[] byteIo = new AvlIoElement.ByteIoElement[byteTotal];
        for (int i = 0; i < byteTotal; i++) {
            final byte ioId = buffer.readByte();
            final byte ioValue = buffer.readByte();
            byteIo[i] = new AvlIoElement.ByteIoElement(ioId, ioValue);
        }
        // Skip shortTotal
        buffer.skipBytes(1);
        // Iterate and read all shorts IO data
        final AvlIoElement.ShortIoElement[] shortIo = new AvlIoElement.ShortIoElement[shortTotal];
        for (int i = 0; i < shortTotal; i++) {
            final byte ioId = buffer.readByte();
            final short ioValue = buffer.readShort();
            shortIo[i] = new AvlIoElement.ShortIoElement(ioId, ioValue);
        }
        // Skip intTotal
        buffer.skipBytes(1);
        // Iterate and read all ints IO data
        final AvlIoElement.IntIoElement[] intIo = new AvlIoElement.IntIoElement[intTotal];
        for (int i = 0; i < intTotal; i++) {
            final byte ioId = buffer.readByte();
            final int ioValue = buffer.readInt();
            intIo[i] = new AvlIoElement.IntIoElement(ioId, ioValue);
        }
        // Skip longTotal
        buffer.skipBytes(1);
        // Iterate and read all longs IO data
        final AvlIoElement.LongIoElement[] longIo = new AvlIoElement.LongIoElement[longTotal];
        for (int i = 0; i < longTotal; i++) {
            final byte ioId = buffer.readByte();
            final long ioValue = buffer.readLong();
            longIo[i] = new AvlIoElement.LongIoElement(ioId, ioValue);
        }
        final AvlIoElement io = new AvlIoElement(eventId, ioTotal, byteIo, shortIo, intIo, longIo);
        return new AvlData(timestamp, priority, gps, io, offset);
    }

    void checkpoint(final DecodingState state) {
        this.state = state;
    }

}
