package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.model.ByteArray;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256 {

    private final MessageDigest sha256Digest;

    public Sha256() {
        try {
            sha256Digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteArray hashIt(String text) {
        byte[] sha256 = sha256Digest.digest(text.getBytes());
        return new ByteArray(sha256);
    }
}
