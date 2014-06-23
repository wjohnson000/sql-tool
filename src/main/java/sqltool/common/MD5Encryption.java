/*
 * Created on Oct 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package sqltool.common;

import javax.crypto.*;
import javax.crypto.spec.*;


/**
 * A utility class that will use the MD5 w/ DES algorithm for encrypting and
 * decrypting string--passwords--based on a user-defined passcode.
 * 
 * @author wjohnson
 */
public class MD5Encryption {
	
	private static final String X_FORM = "PBEWithMD5AndDES";
	private static final byte[] SALT_BYTES = { 0x01, 0x0e, 0x05, 0x09, 0x00, 0x0f, 0x0c, 0x04 };
	private static final String[] HEX_CHAR = { "0", "1", "2", "3", "4", "5", "6", "7",
		"8", "9", "A", "B", "C", "D", "E", "F" };
	
	public String encrypt(String passcode, String target)
	throws Exception {
		byte[] rawBytes = encryptOrDecrypt(Cipher.ENCRYPT_MODE, passcode, target.getBytes());
		return convertToHex(rawBytes);
	}
	
	public String decrypt(String passcode, String target)
	throws Exception {
		byte[] rawBytes = convertFromHex(target);
		byte[] decBytes = encryptOrDecrypt(Cipher.DECRYPT_MODE, passcode, rawBytes);
		return new String(decBytes);
	}
	
	private byte[] encryptOrDecrypt(int mode, String passcode, byte[] inBytes)
	throws Exception {
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT_BYTES, 2);
		PBEKeySpec       pbeKeySpec   = new PBEKeySpec(passcode.toCharArray());
		SecretKeyFactory secKeyFacy   = SecretKeyFactory.getInstance(X_FORM);
		SecretKey        secKey       = secKeyFacy.generateSecret(pbeKeySpec);
		Cipher           cipher       = Cipher.getInstance(X_FORM);
		cipher.init(mode, secKey, pbeParamSpec);
		return cipher.doFinal(inBytes);
	}
	
	private String convertToHex(byte[] rawBytes) {
		StringBuffer res = new StringBuffer(rawBytes.length * 2);
		for (int i=0;  i<rawBytes.length;  i++) {
			res.append(HEX_CHAR[(rawBytes[i] & 0xF0) >>> 4]);
			res.append(HEX_CHAR[rawBytes[i] & 0x0F]);
		}
		return res.toString();
	}
	
	private byte[] convertFromHex(String hexString) {
		byte[] rawBytes = new byte[hexString.length()/2];
		for (int i=0;  i<rawBytes.length;  i++) {
			String hexHex = hexString.substring(i*2, i*2+2);
			int chInt = 32;
			try { chInt = Integer.parseInt(hexHex, 16); } catch (Exception ex) { chInt = 32; }
			rawBytes[i] = (byte)(chInt & 0x000000FF);
		}
		return rawBytes;
	}
}



