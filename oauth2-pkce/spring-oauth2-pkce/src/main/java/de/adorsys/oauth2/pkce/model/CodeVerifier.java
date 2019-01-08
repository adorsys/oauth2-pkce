package de.adorsys.oauth2.pkce.model;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;

public class CodeVerifier extends BaseTypeString {
    public CodeVerifier(String value) {
        super(value);
    }
}
