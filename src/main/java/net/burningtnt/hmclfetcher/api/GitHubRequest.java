package net.burningtnt.hmclfetcher.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class GitHubRequest {
    static final Gson GSON = new Gson();

    private GitHubRequest() {
    }

    enum Type {
        GET, PATCH;

        static {
            try {
                Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                sun.misc.Unsafe u = (sun.misc.Unsafe) unsafeField.get(null);

                HttpURLConnection.setFollowRedirects(true);

                Field methodsField = HttpURLConnection.class.getDeclaredField("methods");
                Object base = u.staticFieldBase(methodsField);
                long offset = u.staticFieldOffset(methodsField);

                String[] methods1 = (String[]) u.getObject(base, offset);
                int l = methods1.length;
                String[] methods2 = new String[l + 1];
                System.arraycopy(methods1, 0, methods2, 0, l);
                methods2[l] = "PATCH";

                u.putObject(base, offset, methods2);
            } catch (Throwable e) {
                throw new AssertionError("Cannot hack HttpURLConnection.", e);
            }
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
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(type.name());
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
