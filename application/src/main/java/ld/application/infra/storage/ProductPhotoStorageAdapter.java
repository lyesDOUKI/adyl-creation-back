package ld.application.infra.storage;

import ld.domain.features.product.photos.ProductPhotoStorageException;
import ld.domain.features.product.photos.ProductPhotoStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class ProductPhotoStorageAdapter implements ProductPhotoStoragePort {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductPhotoStorageAdapter.class);
    private final Path rootDirectory;

    public ProductPhotoStorageAdapter(@Value("${app.photos.storage-path}") String storagePath) {
        this.rootDirectory = Path.of(storagePath);
    }

    @Override
    public String store(UUID productId, String fileName, byte[] content) {
        LOGGER.info("Start store for photo name : {} ...", fileName);
        String extension = extractExtension(fileName);
        String storageKey = "%s%s".formatted(UUID.randomUUID(), extension);

        Path productDirectory = this.rootDirectory.resolve(productId.toString());
        Path targetPath = productDirectory.resolve(storageKey);

        if (!targetPath.startsWith(this.rootDirectory)) {
            throw new ProductPhotoStorageException(
                    "Invalid storage path for file " + fileName,
                    null
            );
        }
        try {
            Files.createDirectories(productDirectory);
            Files.write(targetPath, content);
            LOGGER.info("Success storing file {}", fileName);
            return storageKey;
        } catch (IOException e) {
            throw new ProductPhotoStorageException("Unable to store photo " + fileName, e);
        }
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex).toLowerCase() : "";
    }
}
