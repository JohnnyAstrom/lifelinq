package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.application.RecipeImportFailedException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class RecipeImportHttpFetcher {
    private final HttpClient httpClient;

    RecipeImportHttpFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    FetchedRecipeDocument fetch(String sourceUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "LifeLinq Meals Recipe Import/1.0")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RecipeImportFailedException("Could not fetch recipe URL");
            }
            String body = response.body();
            if (body == null || body.isBlank()) {
                throw new RecipeImportFailedException("Recipe URL returned empty content");
            }
            return new FetchedRecipeDocument(response.uri().toString(), body);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RecipeImportFailedException("Could not fetch recipe URL", ex);
        } catch (IllegalArgumentException ex) {
            throw new RecipeImportFailedException("Could not fetch recipe URL", ex);
        }
    }
}
