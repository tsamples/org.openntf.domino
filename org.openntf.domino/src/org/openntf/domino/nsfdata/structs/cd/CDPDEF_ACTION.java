package org.openntf.domino.nsfdata.structs.cd;

import java.nio.ByteBuffer;

import org.openntf.domino.nsfdata.structs.SIG;
import org.openntf.domino.nsfdata.structs.WSIG;

/**
 * This doesn't appear in the API documentation, but is presumably part of Composite Apps.
 *
 */
public class CDPDEF_ACTION extends CDRecord {

	public static final int SIZE = getFixedStructSize();

	public CDPDEF_ACTION(final CDSignature cdSig) {
		super(new WSIG(cdSig, cdSig.getSize() + SIZE), ByteBuffer.wrap(new byte[SIZE]));
	}

	public CDPDEF_ACTION(final SIG signature, final ByteBuffer data) {
		super(signature, data);
	}

}
