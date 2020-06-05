package de.tiwa.snclient;

import com.google.api.client.util.Key;

import java.util.List;

public class RequestContainer {
    @Key
    public String sync_token;
    @Key
    public Integer limit;
    @Key
    public String cursor_token;
    @Key
    public  List<Item> items;
}
