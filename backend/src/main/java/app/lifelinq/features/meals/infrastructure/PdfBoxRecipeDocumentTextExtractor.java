package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeDocumentTextExtractor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public final class PdfBoxRecipeDocumentTextExtractor implements RecipeDocumentTextExtractor {
    @Override
    public String extract(RecipeDocumentAssetPayload document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        if (looksLikePdf(document)) {
            return extractPdf(document);
        }
        if (looksLikeTextDocument(document)) {
            return normalizeExtractedText(new String(document.content(), StandardCharsets.UTF_8));
        }
        throw new RecipeImportFailedException(
                "That file is not supported for recipe import yet. Try a recipe PDF or text document."
        );
    }

    private String extractPdf(RecipeDocumentAssetPayload document) {
        try (PDDocument pdf = PDDocument.load(document.content())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return normalizeExtractedText(stripper.getText(pdf));
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

    private String normalizeExtractedText(String value) {
        if (value == null) {
            throw new RecipeImportFailedException(
                    "We could not get enough readable recipe content from that file to review."
            );
        }
        String normalized = value
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replaceAll("[\\t\\x0B\\f]+", " ")
                .replaceAll(" +", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
        if (normalized.isEmpty()) {
            throw new RecipeImportFailedException(
                    "We could not get enough readable recipe content from that file to review."
            );
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
