package de.tiwa.snclient;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class AuthParams extends GenericJson {
    @Key
    String identifier;

    @Key
    Long pw_cost;

    @Key
    String pw_nonce;

    @Key
    String version;

    @Key
    String pw_func;

    @Key
    String pw_alg;

    @Key
    Long pw_key_size;
}
