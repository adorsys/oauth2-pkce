package de.adorsys.oauth2.pkce.service;

public class UserInfo {
    private String given_name;
    private String family_name;
    private String name;
    private String sub;
    private String email;
    private boolean email_verified;
    private String person_id;
    
    public String getGiven_name() {
        return given_name;
    }
    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }
    public String getFamily_name() {
        return family_name;
    }
    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSub() {
        return sub;
    }
    public void setSub(String sub) {
        this.sub = sub;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public boolean isEmail_verified() {
        return email_verified;
    }
    public void setEmail_verified(boolean email_verified) {
        this.email_verified = email_verified;
    }
	public String getPerson_id() {
		return person_id;
	}
	public void setPerson_id(String person_id) {
		this.person_id = person_id;
	}
    
    

}
