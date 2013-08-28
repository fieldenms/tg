package ua.com.fielden.platform.gis.gps.server;

public final class Crc16 {
	
	private final int polynomial;
	private int crc = 0;
	
	public Crc16(int polynomial) {
		this.polynomial = polynomial;
	}
	
	public void update(int value) {
		for (int i = 0; i < 8; i++) {
			int add = (crc ^ value) & 1;
			crc >>>= 1;
			value >>>= 1;
			if (add == 1)
				crc ^= polynomial;
		}
		crc &= 0xFFFF;
	}
	
	public void update(byte[] data) {		
		for (byte b : data) {
			update((int)b);
		}
	}
	
	public void update(int offset, int length, byte[] data) {
		for (int i = offset; i < offset + length; i++) {
			update(data[i]);
		}
	}

	public void reset() {
		setCrc(0);
	}
	
	public int getCrc() {
		return crc;
	}

	public void setCrc(int crc) {
		this.crc = crc;
	}

}
