package de.tiwa.snclient;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.io.StringReader;

 class Utils {
    public static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public static JsonFactory JSON_FACTORY = new JacksonFactory();

    public static Content jsonToContent(Item item) throws IOException {
        return Utils.JSON_FACTORY.createJsonObjectParser().parseAndClose(new StringReader(item.decryptet_content),
                Content.class);
    }

    public static String ContentToJson(Content content) throws IOException {
        return Utils.JSON_FACTORY.toPrettyString(content);
    }
}
