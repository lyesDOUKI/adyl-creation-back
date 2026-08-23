package ld.domain.features.product.photos;

import java.util.UUID;

class InMemoryProductPhotoStoragePort implements ProductPhotoStoragePort {

    private int storedCount = 0;
    private String failingFileName = null;

    void failOnFileName() {
        this.failingFileName = "broken.jpg";
    }

    @Override
    public String store(UUID productId, String fileName, byte[] content) {
        if (fileName.equals(this.failingFileName)) {
            throw new ProductPhotoStorageException("Simulated storage failure for " + fileName, null);
        }
        this.storedCount++;
        return productId + "/" + UUID.randomUUID() + "-" + fileName;
    }

    int countStored() {
        return this.storedCount;
    }
}
