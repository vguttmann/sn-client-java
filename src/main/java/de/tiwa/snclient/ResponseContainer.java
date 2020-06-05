package de.tiwa.snclient;

import com.google.api.client.util.Key;

import java.util.List;

public class ResponseContainer {
    @Key
    public List<Item> retrieved_items;

    @Key
    public List<Item> saved_items;

    @Key
    public List<Item> unsaved;

    @Key
    public String sync_token;

    @Key
    public String cursor_token;

}
