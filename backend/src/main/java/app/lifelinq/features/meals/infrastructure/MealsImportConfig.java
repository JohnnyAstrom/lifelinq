package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.contract.RecipeImportPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MealsImportConfig {

    @Bean
    public HttpClient mealsImportHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public RecipeImportHttpFetcher recipeImportHttpFetcher(HttpClient mealsImportHttpClient) {
        return new RecipeImportHttpFetcher(mealsImportHttpClient);
    }

    @Bean
    public RecipeImportHtmlParser recipeImportHtmlParser(ObjectMapper objectMapper) {
        return new RecipeImportHtmlParser(objectMapper);
    }

    @Bean
    public RecipeImportPort recipeImportPort(
            RecipeImportHttpFetcher recipeImportHttpFetcher,
            RecipeImportHtmlParser recipeImportHtmlParser
    ) {
        return new HttpRecipeImportPort(recipeImportHttpFetcher, recipeImportHtmlParser);
    }
}
