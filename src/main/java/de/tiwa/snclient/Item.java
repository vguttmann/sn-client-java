package de.tiwa.snclient;

import com.google.api.client.util.Key;

public class Item {
    @Key
    public String uuid;
    @Key
    public String content;
    @Key
    public String content_type;
    @Key
    public String enc_item_key;
    @Key
    public String auth_hash;
    @Key
    public String created_at;
    @Key
    public String updated_at;
    @Key
    public boolean deleted;

    public String decryptet_content;
}
