package de.adorsys.oauth2.pkce.basetypes;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;

public class CodeChallenge extends BaseTypeString {
    public CodeChallenge(String value) {
        super(value);
    }
}
