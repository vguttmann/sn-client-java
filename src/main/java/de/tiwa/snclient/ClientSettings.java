package de.tiwa.snclient;

public class ClientSettings {
    public String bearerToken;

    public String mk;

    public String ak;

    public String backendUrl;

    public ClientSettings(String bearerToken, String mk, String ak, String backend) {
        this.bearerToken = bearerToken;
        this.mk = mk;
        this.ak = ak;
        this.backendUrl = backend;
    }

}
