package wiki.minecraft.heywiki;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HTTPUtils {
    public static @NotNull String requestUri(URI uri) throws IOException, InterruptedException {
        return requestUri(uri, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    @NotNull
    public static <T> T requestUri(URI uri, HttpResponse.BodyHandler<T> handler) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                                      .proxy(ProxySelector.getDefault())
                                      .followRedirects(HttpClient.Redirect.ALWAYS)
                                      .build();
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        HttpResponse<T> response = client.send(request, handler);
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " " + response.body());
        }

        return response.body();
    }
}
