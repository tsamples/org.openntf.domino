package org.openntf.domino.nsfdata.structs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.openntf.domino.nsfdata.structs.cd.CDSignature;

/**
 * Every CD record begins with a header. There are three types of headers, BSIG, WSIG, and LSIG. The first byte of the header is a signature
 * which identifies the type of the header and the type of the CD record that follows. (ods.h)
 *
 */
public class LSIG extends SIG {
	private static final long serialVersionUID = 1L;

	public static final int SIZE = 6;

	public LSIG(final CDSignature signature, final long length) {
		super(signature, length);
	}

	@Override
	public int getSigLength() {
		return SIZE;
	}

	@Override
	public byte[] getBytes() {
		byte[] result = getSignature().getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
		buffer.position(2);
		buffer.putInt((int) getLength());
		return result;
	}
}