package de.tiwa.snclient;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Map;
import java.util.Objects;

public class StandardNotesClientFactory {

    static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static StandardNotesHighLevelClient createHighLevelClient(ClientSettings clientSettings) {
        clientSettings.backendUrl = (Objects.isNull(clientSettings.backendUrl)) ? Constants.DEFAULT_BACKEND_URL
                : clientSettings.backendUrl;
        return new StandardNotesHighLevelClientImpl(new Client(clientSettings));
    }

    public static StandardNotesLowLevelClient createLowLevelClient(ClientSettings clientSettings) {
        clientSettings.backendUrl = (Objects.isNull(clientSettings.backendUrl)) ? Constants.DEFAULT_BACKEND_URL
                : clientSettings.backendUrl;
        return new StandardNotesLowLevelClientImpl(new Client(clientSettings));
    }

    public static StandardNotesHighLevelClient createHighLevelClient(String mail, String pw) {
        return createHighLevelClient(mail, pw, Constants.DEFAULT_BACKEND_URL);
    }

    public static StandardNotesHighLevelClient createHighLevelClient(String mail, String password, String backendUrl) {
        return new StandardNotesHighLevelClientImpl(createClient(mail, password, backendUrl));
    }

    public static StandardNotesLowLevelClient createLowLevelClient(String mail, String pw) {
        return createLowLevelClient(mail, pw, Constants.DEFAULT_BACKEND_URL);
    }

    public static StandardNotesLowLevelClient createLowLevelClient(String mail, String pw, String backendUrl) {
        return new StandardNotesLowLevelClientImpl(createClient(mail, pw, backendUrl));
    }

    private static Client createClient(String mail, String password, String backendUrl) {
        try {
            HttpRequestFactory requestFactory = Utils.HTTP_TRANSPORT.createRequestFactory((HttpRequest request) -> request.setParser(new JsonObjectParser(Utils.JSON_FACTORY)));
            HttpRequest request = requestFactory
                    .buildGetRequest(new GenericUrl(backendUrl + "/auth/params?email=" + mail));
            Type type = new TypeToken<AuthParams>() {
            }.getType();
            AuthParams authParams = (AuthParams) request.execute().parseAs(type);
            if (authParams.version.equals("003") && authParams.pw_cost < 100000) {
                throw new InternalError();
            }

            String saltSource = authParams.identifier + ":SF:" + authParams.version + ":" + authParams.pw_cost + ":"
                    + authParams.pw_nonce;

            final byte[] salt = StandardNotesClientFactory.digest.digest(saltSource.getBytes(StandardCharsets.UTF_8));
            String hexSalt = Hex.encodeHexString(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), hexSalt.getBytes(), authParams.pw_cost.intValue(),
                    768);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            String key = Hex.encodeHexString(hash);
            String pw = key.substring(0, key.length() / 3);
            String mk = key.substring(key.length() / 3, (key.length() / 3) * 2);
            String ak = key.substring((key.length() / 3) * 2);

            Map<String, Object> content = ImmutableMap.of("email", authParams.identifier, "password",
                    pw);

            request = requestFactory.buildPostRequest(new GenericUrl(backendUrl + "/auth/sign_in"),
                    new JsonHttpContent(Utils.JSON_FACTORY, content));

            type = new TypeToken<SignInReponse>() {
            }.getType();
            SignInReponse signInReponse = (SignInReponse) request.execute().parseAs(type);
            return new Client(new ClientSettings("bearer " + signInReponse.token, mk, ak, backendUrl));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
