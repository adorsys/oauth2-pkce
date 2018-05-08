package de.adorsys.oauth2.pkce.context;

import de.adorsys.oauth2.pkce.basetypes.*;

public class Oauth2PkceContext {

    private State state;
    private Code code;
    private CodeVerifier codeVerifier;
    private CodeChallenge codeChallenge;
    private Nonce nonce;

    public State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    public Code getCode() {
        return code;
    }

    void setCode(Code code) {
        this.code = code;
    }

    public CodeVerifier getCodeVerifier() {
        return codeVerifier;
    }

    void setCodeVerifier(CodeVerifier codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    public CodeChallenge getCodeChallenge() {
        return codeChallenge;
    }

    void setCodeChallenge(CodeChallenge codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public Nonce getNonce() {
        return nonce;
    }

    void setNonce(Nonce nonce) {
        this.nonce = nonce;
    }
}
