package sv.edu.uca.delivery.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sv.edu.uca.delivery.backend.exception.BusinessException;
import sv.edu.uca.delivery.backend.config.UploadProperties;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UploadProperties properties;

    public String storeRestaurantImage(UUID restaurantId, MultipartFile file, String currentPath) {
        return store(file, "restaurants", "restaurant-" + restaurantId, properties.getRestaurantMaxWidth(), currentPath);
    }

    public String storeProductImage(UUID productId, MultipartFile file, String currentPath) {
        return store(file, "products", "product-" + productId, properties.getProductMaxWidth(), currentPath);
    }

    public void delete(String publicPath) {
        if (publicPath == null || publicPath.isBlank()) {
            return;
        }
        try {
            Path target = resolvePublicPath(publicPath);
            if (target != null) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ignored) {
            // File cleanup should not break business operations.
        }
    }

    private String store(MultipartFile file, String folder, String baseName, int maxWidth, String currentPath) {
        validate(file);
        BufferedImage original = readImage(file);
        BufferedImage resized = resizeIfNeeded(original, maxWidth);
        EncodedImage encoded = encode(resized);

        Path directory = root().resolve(folder).normalize();
        Path target = directory.resolve(baseName + "." + encoded.extension()).normalize();
        if (!target.startsWith(root())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid image storage path");
        }

        try {
            Files.createDirectories(directory);
            Files.write(target, encoded.bytes());
            deleteDifferent(currentPath, toPublicPath(folder, target.getFileName().toString()));
            return toPublicPath(folder, target.getFileName().toString());
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store image");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Image exceeds maximum allowed size");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only JPG, PNG or WEBP images are allowed");
        }
        String filename = file.getOriginalFilename();
        String extension = filename == null || !filename.contains(".")
                ? ""
                : filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only JPG, PNG or WEBP images are allowed");
        }
    }

    private BufferedImage readImage(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid image content");
            }
            return image;
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid image content");
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage original, int maxWidth) {
        if (original.getWidth() <= maxWidth) {
            return toRgb(original);
        }
        double scale = (double) maxWidth / original.getWidth();
        int targetHeight = Math.max(1, (int) Math.round(original.getHeight() * scale));
        BufferedImage resized = new BufferedImage(maxWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(original, 0, 0, maxWidth, targetHeight, null);
        graphics.dispose();
        return resized;
    }

    private BufferedImage toRgb(BufferedImage original) {
        if (original.getType() == BufferedImage.TYPE_INT_RGB) {
            return original;
        }
        BufferedImage rgb = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        graphics.drawImage(original, 0, 0, null);
        graphics.dispose();
        return rgb;
    }

    private EncodedImage encode(BufferedImage image) {
        EncodedImage webp = tryEncode(image, "webp", "webp");
        if (webp != null) {
            return webp;
        }
        EncodedImage jpeg = tryEncode(image, "jpeg", "jpg");
        if (jpeg != null) {
            return jpeg;
        }
        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No image writer available");
    }

    private EncodedImage tryEncode(BufferedImage image, String format, String extension) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            return null;
        }
        ImageWriter writer = writers.next();
        try (java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(Math.max(0.1f, Math.min(properties.getQuality(), 1.0f)));
            }
            writer.write(null, new IIOImage(image, null, null), params);
            return new EncodedImage(output.toByteArray(), extension);
        } catch (IOException | RuntimeException ex) {
            return null;
        } finally {
            writer.dispose();
        }
    }

    private void deleteDifferent(String oldPublicPath, String newPublicPath) {
        if (oldPublicPath != null && !oldPublicPath.equals(newPublicPath)) {
            delete(oldPublicPath);
        }
    }

    private Path resolvePublicPath(String publicPath) {
        String prefix = normalizePublicPath(properties.getPublicPath()) + "/";
        if (!publicPath.startsWith(prefix)) {
            return null;
        }
        Path target = root().resolve(publicPath.substring(prefix.length())).normalize();
        return target.startsWith(root()) ? target : null;
    }

    private String toPublicPath(String folder, String filename) {
        return normalizePublicPath(properties.getPublicPath()) + "/" + folder + "/" + filename;
    }

    private Path root() {
        return Path.of(properties.getRootPath()).toAbsolutePath().normalize();
    }

    private String normalizePublicPath(String publicPath) {
        if (publicPath == null || publicPath.isBlank()) {
            return "/uploads";
        }
        String normalized = publicPath.startsWith("/") ? publicPath : "/" + publicPath;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private record EncodedImage(byte[] bytes, String extension) {
    }
}
