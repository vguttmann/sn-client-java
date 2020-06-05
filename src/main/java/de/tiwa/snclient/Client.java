package de.tiwa.snclient;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.reflect.TypeToken;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Client {
    private final ClientSettings clientSettings;

    public Client(ClientSettings clientSettings) {
        this.clientSettings = clientSettings;
    }

    ClientSettings getClientSettings() {
        return clientSettings;
    }

    private void decryptContentInList(List<Item> items) {
        for (Item item : items) {
            try {
                if (item.content.isEmpty() || item.enc_item_key.isEmpty()) {
                    continue;
                }
                if (!item.content.startsWith("003")) {
                    throw new InternalError(item.uuid + " not version 003");
                }
                String itemKey = CryptoHelper.decryptString(item.enc_item_key, item.uuid, clientSettings.mk,
                        clientSettings.ak);
                item.decryptet_content = CryptoHelper.decryptString(item.content, item.uuid,
                        itemKey.substring(0, itemKey.length() / 2),
                        itemKey.substring(itemKey.length() / 2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ResponseContainer decryptSyncContainer(ResponseContainer responseContainer) {
        decryptContentInList(responseContainer.retrieved_items);
        decryptContentInList(responseContainer.saved_items);
        decryptContentInList(responseContainer.unsaved);
        return responseContainer;
    }

    private void encryptContentInList(List<Item> items) {
        for (Item item : items) {
            try {
                if (item.deleted) {
                    continue;
                }
                SecureRandom random = new SecureRandom();
                byte[] item_key = new byte[64];
                random.nextBytes(item_key);
                String encodeHexItemKey = Hex.encodeHexString(item_key);
                item.uuid = (Objects.isNull(item.uuid)) ? UUID.randomUUID().toString() : item.uuid;
                item.content = CryptoHelper.encryptString(item.decryptet_content,
                        encodeHexItemKey.substring(0, encodeHexItemKey.length() / 2),
                        encodeHexItemKey.substring(encodeHexItemKey.length() / 2),
                        item.uuid);
                item.enc_item_key = CryptoHelper.encryptString(encodeHexItemKey, clientSettings.mk, clientSettings.ak,
                        item.uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ResponseContainer syncNotes(RequestContainer requestContainer) {
        if (requestContainer.items != null) {
            encryptContentInList(requestContainer.items);
        }
        try {
            HttpRequestFactory requestFactory = Utils.HTTP_TRANSPORT.createRequestFactory((HttpRequest request) -> request.setParser(new JsonObjectParser(Utils.JSON_FACTORY)));

            HttpRequest request = requestFactory.buildPostRequest(
                    new GenericUrl(clientSettings.backendUrl + "/items/sync"),
                    new JsonHttpContent(Utils.JSON_FACTORY, requestContainer));

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setAuthorization(clientSettings.bearerToken);
            request.setHeaders(httpHeaders);
            Type type = new TypeToken<ResponseContainer>() {
            }.getType();
            ResponseContainer responseContainer = (ResponseContainer) request.execute().parseAs(type);
            return decryptSyncContainer(responseContainer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
