package wiki.minecraft.heywiki.util;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

/**
 * A utility class for making HTTP requests.
 */
public class HttpUtil {
    private final static Logger LOGGER = LogUtils.getLogger();

    /**
     * Sends a GET request to the given URI and returns the response body as a string.
     *
     * @param uri The URI to send the request to.
     * @return The response body as a string.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the request is interrupted.
     * @see HttpClient#send(HttpRequest, HttpResponse.BodyHandler)
     * @see #request(URI, HttpResponse.BodyHandler)
     */
    public static @NotNull String request(URI uri) throws IOException, InterruptedException {
        return request(uri, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    /**
     * Sends a GET request to the given URI and returns the response body using the given handler.
     *
     * @param uri     The URI to send the request to.
     * @param handler The response body handler.
     * @return The response body.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the request is interrupted.
     * @see HttpClient#send(HttpRequest, HttpResponse.BodyHandler)
     * @see #request(URI)
     */
    @NotNull
    public static <T> T request(URI uri, HttpResponse.BodyHandler<T> handler)
            throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newBuilder()
                                           .proxy(ProxySelector.getDefault())
                                           .followRedirects(HttpClient.Redirect.ALWAYS)
                                           .build()) {
            HttpRequest request = HttpRequest.newBuilder(uri)
                                             .GET()
                                             .header("User-Agent",
                                                     "HeyWikiMod (+https://github.com/mc-wiki/minecraft-mod-heywiki)")
                                             .build();

            HttpResponse<T> response = client.send(request, handler);
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + " " + response.body());
            }

            return response.body();
        }
    }

    public static URI uriWithQuery(URI uri, String query) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static URI uriWithPath(URI uri, String path) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), path, uri.getQuery(), uri.getFragment());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<byte[]> loadAndCacheFile(String url) {
        return CompletableFuture.supplyAsync(() -> {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            String hash = Hex.encodeHexString(md.digest(url.getBytes(StandardCharsets.UTF_8)));
            String tempDir = System.getProperty("java.io.tmpdir");

            Path path = FileSystems.getDefault().getPath(tempDir, "/heywiki/", hash);
            if (Files.exists(path)) {
                try {
                    return Files.readAllBytes(path);
                } catch (IOException e) {
                    LOGGER.error("Failed to fetch file", e);
                }
            } else {
                try {
                    byte[] fileData = request(URI.create(url), HttpResponse.BodyHandlers.ofByteArray());

                    Path parentDir = path.getParent();
                    if (!Files.exists(parentDir)) {
                        Files.createDirectories(parentDir);
                    }
                    Files.write(path, fileData);
                    return fileData;
                } catch (Exception e) {
                    LOGGER.error("Failed to fetch image", e);
                }
            }

            return null;
        }, Util.nonCriticalIoPool());
    }

    /**
     * Encodes a URL using UTF-8.
     *
     * <p>Unlike {@link URLEncoder#encode(String, String)}, this method replaces spaces with "%20" instead of "+".
     *
     * @param url The URL to encode.
     * @return The encoded URL.
     */
    public static String encodeUrl(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }
}
