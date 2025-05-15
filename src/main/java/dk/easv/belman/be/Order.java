package dk.easv.belman.be;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Long id;
    private String orderNumber;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private List<Photo> photos;
    private Boolean isSigned;

    public Order(Long id, String productNumber, LocalDateTime createdAt, Boolean isDeleted, List<Photo> photos, Boolean isSigned)
    {
        this.id = id;
        this.orderNumber = productNumber;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.photos = photos;
        this.isSigned = isSigned;
    }

    public Long getId()
    {
        return id;
    }

    public String getOrderNumber()
    {
        return orderNumber;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public Boolean getIsDeleted()
    {
        return isDeleted;
    }

    public List<Photo> getPhotos()
    {
        return photos;
    }

    public Boolean getIsSigned()
    {
        return isSigned;
    }
}
