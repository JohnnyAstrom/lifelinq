package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.RecipeImageTextExtractor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;

public final class Tess4jRecipeImageTextExtractor implements RecipeImageTextExtractor {
    private static final String OCR_LANGUAGE = "eng";
    private final String tessdataPath;

    public Tess4jRecipeImageTextExtractor() {
        File tessdataDirectory = LoadLibs.extractTessResources("tessdata");
        this.tessdataPath = tessdataDirectory.getAbsolutePath();
    }

    @Override
    public String extractText(List<BufferedImage> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("images must not be empty");
        }

        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage(OCR_LANGUAGE);
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);

            StringBuilder builder = new StringBuilder();
            for (BufferedImage image : images) {
                if (image == null) {
                    continue;
                }
                String text = tesseract.doOCR(image);
                if (text != null && !text.isBlank()) {
                    if (builder.length() > 0) {
                        builder.append("\n\n");
                    }
                    builder.append(text.trim());
                }
            }
            return builder.toString();
        } catch (TesseractException ex) {
            throw new RecipeImportFailedException(
                    "We could not read enough from that photo. Try another recipe photo or document.",
                    ex
            );
        }
    }
}
