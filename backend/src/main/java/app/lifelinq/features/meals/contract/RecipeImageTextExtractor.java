package app.lifelinq.features.meals.contract;

import java.awt.image.BufferedImage;
import java.util.List;

public interface RecipeImageTextExtractor {
    String extractText(List<BufferedImage> images);
}
