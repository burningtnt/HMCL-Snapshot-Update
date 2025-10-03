package net.burningtnt.hmclfetcher.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.burningtnt.hmclfetcher.utils.TrustedLookup;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.invoke.VarHandle;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class GitHubRequestUtils {
    static final Gson GSON = new Gson();

    private GitHubRequestUtils() {
    }

    enum Type {
        GET, PATCH;

        private static final VarHandle METHOD;

        static {
            try {
                METHOD = TrustedLookup.getInstance().findVarHandle(HttpURLConnection.class, "method", String.class).withInvokeExactBehavior();
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public void apply(HttpURLConnection connection) {
            METHOD.set(connection, name());
        }
    }

    static <T> T ofStructuredResult(GitHubAPI apiHandle, Type type, String url, TypeToken<T> resultType) throws IOException {
        try (Reader reader = new InputStreamReader(buildConnection(apiHandle, type, url).getInputStream())) {
            return GSON.fromJson(reader, resultType);
        }
    }

    static <T> T ofStructuredResult(GitHubAPI apiHandle, Type type, String url, Class<T> resultType) throws IOException {
        try (Reader reader = new InputStreamReader(buildConnection(apiHandle, type, url).getInputStream())) {
            return GSON.fromJson(reader, resultType);
        }
    }

    static <T> T ofStructuredResult(GitHubAPI apiHandle, Type type, String url, Map<String, String> query, TypeToken<T> resultType) throws IOException {
        return ofStructuredResult(apiHandle, type, withQuery(url, query), resultType);
    }

    static <T> T ofStructuredResult(GitHubAPI apiHandle, Type type, String url, Map<String, String> query, Class<T> resultType) throws IOException {
        return ofStructuredResult(apiHandle, type, withQuery(url, query), resultType);
    }

    static InputStream ofStream(GitHubAPI apiHandle, Type type, String url) throws IOException {
        return buildConnection(apiHandle, type, url).getInputStream();
    }

    static <B> void ofSend(GitHubAPI apiHandle, Type type, String url, B body) throws IOException {
        HttpURLConnection connection = buildConnection(apiHandle, type, url);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

        connection.setDoOutput(true);
        try (Writer writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            GSON.toJson(body, writer);
        }

        connection.getInputStream().transferTo(OutputStream.nullOutputStream());
    }

    private static HttpURLConnection buildConnection(GitHubAPI apiHandle, Type type, String url) throws IOException {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URI(url).toURL().openConnection();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        type.apply(connection);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("Authorization", apiHandle.token);
        connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        connection.setInstanceFollowRedirects(true);

        return connection;
    }

    private static String withQuery(String baseURL, Map<String, String> queryArgs) {
        if (queryArgs.isEmpty()) {
            return baseURL;
        }

        StringBuilder stringBuilder = new StringBuilder(baseURL);
        stringBuilder.append('?');
        for (Map.Entry<String, String> arg : queryArgs.entrySet()) {
            stringBuilder.append(encodeURL(arg.getKey()));
            stringBuilder.append('=');
            stringBuilder.append(encodeURL(arg.getValue()));
            stringBuilder.append('&');
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    private static String encodeURL(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}
