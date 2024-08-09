package wiki.minecraft.heywiki;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * A utility class for making HTTP requests.
 */
public class HTTPUtils {
    /**
     * Sends a GET request to the given URI and returns the response body as a string.
     *
     * @param uri The URI to send the request to.
     * @return The response body as a string.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the request is interrupted.
     * @see HttpClient#send(HttpRequest, HttpResponse.BodyHandler)
     * @see #requestUri(URI, HttpResponse.BodyHandler)
     */
    public static @NotNull String requestUri(URI uri) throws IOException, InterruptedException {
        return requestUri(uri, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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
     * @see #requestUri(URI)
     */
    @NotNull
    public static <T> T requestUri(URI uri, HttpResponse.BodyHandler<T> handler)
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
}
