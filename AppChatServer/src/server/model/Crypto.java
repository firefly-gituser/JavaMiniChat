package server.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto {
	public static String getSHA256(String original) {
		MessageDigest digest;
		byte[] buffer = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			buffer = digest.digest(original.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		
		StringBuilder builder = new StringBuilder(2*buffer.length);
		
		for(int i=0; i<buffer.length; i++) {
			String hexString  = Integer.toHexString(0xff & buffer[i]);
			if(hexString.length()==1)
				builder.append('0');
			builder.append(hexString);
		}
		
		return builder.toString();
	}
}
