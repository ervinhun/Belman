package dk.easv.belman.be;

import java.time.LocalDateTime;

public class Product {
    private long id;
    private String productNumber;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    public Product(long id, String productNumber, LocalDateTime createdAt, boolean isDeleted) {
        this.id = id;
        this.productNumber = productNumber;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
    }

    public Product(String productNumber) {
        this.productNumber = productNumber;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getProductNumber() {
        return productNumber;
    }
    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", productNumber='" + productNumber + '\'' +
                ", createdAt=" + createdAt +
                ", isDeleted=" + isDeleted +
                '}';
    }

}
