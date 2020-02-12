package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.model.ByteArray;

import java.security.SecureRandom;

public class RandomBytesGenerator {

    public ByteArray generate(int size) {
        byte[] generatedBytes = new byte[size];

        SecureRandom random = new SecureRandom();
        random.nextBytes(generatedBytes);

        return new ByteArray(generatedBytes);
    }
}
