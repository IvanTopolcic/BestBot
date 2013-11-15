package org.bestever.utilities;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Encryption {
	
	public Encryption() throws NoSuchAlgorithmException {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		keygen.initialize(1024);
		KeyPair key = keygen.generateKeyPair();
		System.out.println(Arrays.toString(key.getPublic().getEncoded()));
		System.out.println(Arrays.toString(key.getPrivate().getEncoded()));
	}
}
