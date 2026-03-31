package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentImportAnalysis;
import app.lifelinq.features.meals.contract.RecipeDocumentImportStrategy;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

public final class PdfBoxRecipeDocumentTextExtractor implements RecipeDocumentTextExtractor {
    @Override
    public RecipeDocumentImportAnalysis analyze(RecipeDocumentAssetPayload document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        if (looksLikePdf(document)) {
            return analyzePdf(document);
        }
        if (looksLikeTextDocument(document)) {
            return new RecipeDocumentImportAnalysis(
                    RecipeDocumentImportStrategy.TEXT_BACKED_DOCUMENT,
                    new String(document.content(), StandardCharsets.UTF_8)
            );
        }
        throw new RecipeImportFailedException(
                "That file is not supported for recipe import yet. Try a recipe PDF or text document."
        );
    }

    private RecipeDocumentImportAnalysis analyzePdf(RecipeDocumentAssetPayload document) {
        try (PDDocument pdf = PDDocument.load(document.content())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String normalizedText = normalizeExtractedText(stripper.getText(pdf));
            int meaningfulCharacterCount = countMeaningfulCharacters(normalizedText);
            int meaningfulLineCount = countMeaningfulLines(normalizedText);
            boolean hasEmbeddedImages = hasEmbeddedImages(pdf);

            RecipeDocumentImportStrategy strategy = classifyPdf(
                    pdf.getNumberOfPages(),
                    meaningfulCharacterCount,
                    meaningfulLineCount,
                    hasEmbeddedImages
            );
            return new RecipeDocumentImportAnalysis(strategy, normalizedText);
        } catch (IOException ex) {
            throw new RecipeImportFailedException(
                    "We could not read that PDF. Try another recipe PDF or document.",
                    ex
            );
        }
    }

    private boolean looksLikePdf(RecipeDocumentAssetPayload document) {
        String mimeType = normalize(document.mimeType());
        String originalFilename = normalize(document.originalFilename());
        byte[] content = document.content();
        boolean hasPdfHeader = content.length >= 4
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F';
        return "application/pdf".equals(mimeType)
                || (originalFilename != null && originalFilename.endsWith(".pdf"))
                || hasPdfHeader;
    }

    private boolean looksLikeTextDocument(RecipeDocumentAssetPayload document) {
        String mimeType = normalize(document.mimeType());
        String originalFilename = normalize(document.originalFilename());
        if (mimeType != null && mimeType.startsWith("text/")) {
            return true;
        }
        if (originalFilename == null) {
            return false;
        }
        return originalFilename.endsWith(".txt")
                || originalFilename.endsWith(".text")
                || originalFilename.endsWith(".md");
    }

    private RecipeDocumentImportStrategy classifyPdf(
            int pageCount,
            int meaningfulCharacterCount,
            int meaningfulLineCount,
            boolean hasEmbeddedImages
    ) {
        boolean textBacked = meaningfulCharacterCount >= 160
                || (meaningfulCharacterCount >= 80 && meaningfulLineCount >= 4)
                || meaningfulLineCount >= Math.max(4, pageCount * 4);
        if (textBacked) {
            return RecipeDocumentImportStrategy.TEXT_BACKED_DOCUMENT;
        }
        if (hasEmbeddedImages) {
            return RecipeDocumentImportStrategy.IMAGE_LIKE_DOCUMENT;
        }
        return RecipeDocumentImportStrategy.TOO_WEAK_TO_CLASSIFY;
    }

    private boolean hasEmbeddedImages(PDDocument pdf) throws IOException {
        for (PDPage page : pdf.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) {
                continue;
            }
            for (COSName name : resources.getXObjectNames()) {
                PDXObject object = resources.getXObject(name);
                if (object instanceof PDImageXObject) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeExtractedText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ');
        normalized = normalized.replaceAll("[\\t\\x0B\\f]+", " ");
        normalized = normalized.replaceAll(" +", " ");
        normalized = normalized.replaceAll("\\n{3,}", "\n\n");
        normalized = normalized.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private int countMeaningfulCharacters(String value) {
        if (value == null) {
            return 0;
        }
        int count = 0;
        for (int index = 0; index < value.length(); index += 1) {
            char character = value.charAt(index);
            if (Character.isLetterOrDigit(character)) {
                count += 1;
            }
        }
        return count;
    }

    private int countMeaningfulLines(String value) {
        if (value == null) {
            return 0;
        }
        int count = 0;
        for (String line : value.split("\\R")) {
            if (!line.trim().isEmpty()) {
                count += 1;
            }
        }
        return count;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
