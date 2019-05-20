package de.adorsys.oauth2.pkce.model;

import de.adorsys.sts.cryptoutils.BaseTypeString;

public class State extends BaseTypeString {
    public State(String value) {
        super(value);
    }
}
