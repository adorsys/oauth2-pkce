package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.model.ByteArray;

import java.util.Random;

public class RandomBytesGenerator {

    public ByteArray generate(int size) {
        byte[] generatedBytes = new byte[size];

        Random random = new Random();
        random.nextBytes(generatedBytes);

        return new ByteArray(generatedBytes);
    }
}
