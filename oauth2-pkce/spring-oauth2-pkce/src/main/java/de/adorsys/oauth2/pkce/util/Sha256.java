package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.basetypes.ByteArray;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256 {

    public ByteArray hashIt(String text) {
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }


        byte[] sha256 = digest.digest(text.getBytes());

        return new ByteArray(sha256);
    }
}
