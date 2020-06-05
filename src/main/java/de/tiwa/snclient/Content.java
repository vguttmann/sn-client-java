package de.tiwa.snclient;

import com.google.api.client.util.Key;

import java.util.List;

public class Content {
    @Key
    public String text;

    @Key
    public String title;

    @Key
    public List<Reference> references;
}
