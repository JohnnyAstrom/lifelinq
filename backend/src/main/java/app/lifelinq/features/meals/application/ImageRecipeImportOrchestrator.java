package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeDocumentAssetPayload;
import app.lifelinq.features.meals.contract.RecipeImageAssetPayload;
import app.lifelinq.features.meals.contract.RecipeImageTextExtractor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

final class ImageRecipeImportOrchestrator {
    private static final int MAX_PDF_PAGES_FOR_IMAGE_IMPORT = 3;
    private static final float PDF_RENDER_DPI = 220f;
    private static final int MAX_IMAGE_WIDTH = 1800;

    private final RecipeImageTextExtractor imageTextExtractor;
    private final ImageRecipeImportShaper imageImportShaper;

    ImageRecipeImportOrchestrator(
            RecipeImageTextExtractor imageTextExtractor,
            ImageRecipeImportShaper imageImportShaper
    ) {
        if (imageTextExtractor == null) {
            throw new IllegalArgumentException("imageTextExtractor must not be null");
        }
        if (imageImportShaper == null) {
            throw new IllegalArgumentException("imageImportShaper must not be null");
        }
        this.imageTextExtractor = imageTextExtractor;
        this.imageImportShaper = imageImportShaper;
    }

    ParsedRecipeImportData extractFromImageAsset(
            RecipeAssetIntakeReference reference,
            RecipeImageAssetPayload imageAsset
    ) {
        return shapeFromImages(reference, readImagePayload(imageAsset));
    }

    ParsedRecipeImportData extractFromImageLikeDocument(
            RecipeAssetIntakeReference reference,
            RecipeDocumentAssetPayload document
    ) {
        return shapeFromImages(reference, renderDocumentToImages(document));
    }

    private ParsedRecipeImportData shapeFromImages(
            RecipeAssetIntakeReference reference,
            List<BufferedImage> sourceImages
    ) {
        if (sourceImages.isEmpty()) {
            throw createUnreadableImportFailure(reference);
        }

        String extractedText = imageTextExtractor.extractText(sourceImages);
        if (normalizeOptionalText(extractedText) == null) {
            throw createUnreadableImportFailure(reference);
        }
        return imageImportShaper.shape(reference, extractedText);
    }

    private List<BufferedImage> readImagePayload(RecipeImageAssetPayload imageAsset) {
        if (imageAsset == null) {
            throw new IllegalArgumentException("imageAsset must not be null");
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAsset.content()));
            if (image == null) {
                throw createUnreadableImportFailure(RecipeAssetIntakeKind.IMAGE);
            }
            return List.of(prepareForOcr(image));
        } catch (IOException ex) {
            throw new RecipeImportFailedException(
                    "We could not open that photo. Try another recipe photo.",
                    ex
            );
        }
    }

    private List<BufferedImage> renderDocumentToImages(RecipeDocumentAssetPayload document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        if (!looksLikePdf(document)) {
            throw createUnreadableImportFailure(RecipeAssetIntakeKind.DOCUMENT);
        }

        try (PDDocument pdf = PDDocument.load(document.content())) {
            PDFRenderer renderer = new PDFRenderer(pdf);
            int pageCount = Math.min(pdf.getNumberOfPages(), MAX_PDF_PAGES_FOR_IMAGE_IMPORT);
            List<BufferedImage> images = new ArrayList<>(pageCount);
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex += 1) {
                images.add(prepareForOcr(renderer.renderImageWithDPI(pageIndex, PDF_RENDER_DPI, ImageType.GRAY)));
            }
            return List.copyOf(images);
        } catch (IOException ex) {
            throw new RecipeImportFailedException(
                    "We could not open that file. Try another recipe PDF or document.",
                    ex
            );
        }
    }

    private boolean looksLikePdf(RecipeDocumentAssetPayload document) {
        String mimeType = normalizeLowercase(document.mimeType());
        String filename = normalizeLowercase(document.originalFilename());
        byte[] content = document.content();
        boolean hasPdfHeader = content.length >= 4
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F';
        return "application/pdf".equals(mimeType)
                || (filename != null && filename.endsWith(".pdf"))
                || hasPdfHeader;
    }

    private BufferedImage prepareForOcr(BufferedImage source) {
        BufferedImage scaled = source;
        if (source.getWidth() > MAX_IMAGE_WIDTH) {
            int targetWidth = MAX_IMAGE_WIDTH;
            int targetHeight = Math.max(1, Math.round((source.getHeight() * targetWidth) / (float) source.getWidth()));
            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = resized.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();
            scaled = resized;
        }

        BufferedImage grayscale = new BufferedImage(
                scaled.getWidth(),
                scaled.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(scaled, grayscale);
        return grayscale;
    }

    private RecipeImportFailedException createUnreadableImportFailure(RecipeAssetIntakeReference reference) {
        return createUnreadableImportFailure(reference.kind());
    }

    private RecipeImportFailedException createUnreadableImportFailure(RecipeAssetIntakeKind kind) {
        return new RecipeImportFailedException(
                kind == RecipeAssetIntakeKind.IMAGE
                        ? "We could not find enough readable recipe content in that photo to review."
                        : "We could not find enough readable recipe content in that file to review."
        );
    }

    private String normalizeLowercase(String value) {
        String normalized = normalizeOptionalText(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
