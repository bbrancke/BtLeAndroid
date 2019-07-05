package com.nerdlinger.btle.Utilities;

import android.util.Log;

public class HexConverter {
	// Java has no unsigned types (including bytes)
	//   so 0x99 has its high-order bit carried leftward
	//   and becomes 0xffff_ff99 ( -130 or so).
	// We have to implicitly do &= 0xff on all of these
	//   which is why getUint16Le() calls getUint8():
	public int getUint8(final byte[] data, int index) {
		int val = data[index];
		val &= 0xff;
		return val;
	}

	// Get Little-Endian value at (data + index)
	// Ex: 71 00 ==> 0x0071 = 113 decimal
	public int getUint16Le(final byte[] data, int index) {
		int highOrder = getUint8(data, index + 1);
		highOrder <<= 8;
		int lowOrder = getUint8(data, index);
		int val = highOrder | lowOrder;
		return val;
	}

	public int getSint16Le(final byte[] data, int index) {
		int highorder = data[index + 1];
		highorder <<= 8;
		int val = highorder | getUint8(data, index);
		return val;
	}
	// ==============================================
	// Byte Array <--> String converters:
	public byte[] HexStringToByteArray(String s) {
		// "11 22 33 44" -> { 0x11, 0x22, 0x33, 0x44 }
		String oneByte = "?";
		try {

			if (!s.endsWith(" ")) {
				s += " ";
			}

			byte data[] = new byte[s.length() / 3];
			int index = 0;
			for (int i = 0; i < s.length(); i += 3) {
				oneByte = "0x" + s.charAt(i) + s.charAt(i + 1);  // "11"
				Integer val = Integer.decode(oneByte);
				data[index] = val.byteValue();
				index++;
			}
			return data;
		}
		catch (Exception ex) {
			Log.e("OneReading", "Parse [" + s + "' failed on ["  + oneByte +"]: " + ex.getMessage());
			return null;
		}
	}

	public String _byteArrayToString(byte[] val, String seperator) {
		int i;
		int theByte;
		StringBuilder sb = new StringBuilder();

		theByte = getUint8(val, 0);
		sb.append(String.format("%02x", theByte));

		for (i = 1; i < val.length; i++) {
			sb.append(seperator);
			theByte = getUint8(val, i);
			sb.append(String.format("%02x", theByte));
		}
		return sb.toString();
	}

	public String ByteArrayToString(byte[] val) {
		return _byteArrayToString(val, " ");
	}

	public String ByteArrayToBdAddr(byte[] val) {
		return _byteArrayToString(val, ":");
	}
}
