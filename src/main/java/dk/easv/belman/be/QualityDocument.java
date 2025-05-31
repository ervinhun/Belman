package dk.easv.belman.be;

import java.time.LocalDateTime;
import java.util.UUID;

public class QualityDocument {
    private long id;
    private UUID generatedBy;
    private long productId;
    private LocalDateTime generatedAt;
    private String qcDocPath;
    private String generatedByName;


    public QualityDocument(long id, String generatedByName, long productId, LocalDateTime generatedAt) {
        this.id = id;
        this.generatedByName = generatedByName;
        this.productId = productId;
        this.generatedAt = generatedAt;
        this.generatedBy = null; // UUID will be set later
        this.qcDocPath = null; // Path is not used anymore
    }
    public QualityDocument(UUID generatedBy, long productId) {
        this.generatedBy = generatedBy;
        this.productId = productId;
        this.generatedAt = LocalDateTime.now();
        this.qcDocPath = null; // Path is not used anymore
        this.generatedByName = null; // Name will be set later
        this.id = 0; // ID will be set later
    }

    public long getId() {
        return id;
    }

    public UUID getGeneratedBy() {
        return generatedBy;
    }

    public long getProductId() {
        return productId;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public String getQcDocPath() {
        return qcDocPath;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setGeneratedBy(UUID generatedBy) {
        this.generatedBy = generatedBy;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public void setQcDocPath(String qcDocPath) {
        this.qcDocPath = qcDocPath;
    }

    public void setGeneratedByName(String generatedByName) {
        this.generatedByName = generatedByName;
    }
    public String getGeneratedByName() {
        return generatedByName;
    }
}
