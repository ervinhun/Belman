package dk.easv.belman.dal;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.exceptions.BelmanException;

import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OrderManager extends DALManagerBase {

    public byte[] getPdfFromDb(String productNumber) {
        long productId = getProductIdFromProductNumber(productNumber);
        String sql = "SELECT document FROM dbo.QualityCheckDoc WHERE product_id = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBytes("document");
            }
        } catch (SQLException e) {
            logger.error("Error fetching PDF from DB: {}", e.getMessage());
        }
        return new byte[0];
    }

    public long getProductIdFromProductNumber(String productNumber) {
        String sql = "SELECT id FROM dbo.Products WHERE product_number = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, productNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException _) {
            throw new BelmanException("Error fetching product ID");
        }
        return 0;
    }

    public boolean signQualityDocument(String orderNumber, UUID userId) {
        int noOfTableUpdated = 0;
        long productId = getProductIdFromProductNumber(orderNumber);

        //Update photos that they got signed
        String sqlPhotos = "UPDATE dbo.Photos SET is_signed = 1 WHERE product_id = ?";
        String sqlProduct = "UPDATE dbo.Products SET is_signed = 1 WHERE id = ?";


        // Merge statement to update or insert into QualityChecks table
        String sqlUpdateQuailityCheck = "MERGE dbo.QualityChecks AS target " +
                                        "USING (SELECT ? AS product_id, ? AS info, ? AS signed_at, ? AS signed_by, 1 AS is_signed) AS source " +
                                        "    ON target.product_id = source.product_id " +
                                        "WHEN MATCHED THEN " +
                                        "    UPDATE SET " +
                                        "        is_signed = 1, " +
                                        "        signed_at = source.signed_at, " +
                                        "        signed_by = source.signed_by, " +
                                        "        info = source.info " +
                                        "WHEN NOT MATCHED THEN " +
                                        "    INSERT (product_id, info, signed_at, signed_by, is_signed) " +
                                        "    VALUES (source.product_id, source.info, source.signed_at, source.signed_by, 1); ";

        try (Connection c = connectionManager.getConnection();
             PreparedStatement psPhotos = c.prepareStatement(sqlPhotos);
             PreparedStatement psUpdateQualityCheck = c.prepareStatement(sqlUpdateQuailityCheck);
             PreparedStatement psProduct = c.prepareStatement(sqlProduct)) {

            psPhotos.setLong(1, productId);
            int rowsUpdated = psPhotos.executeUpdate();
            if (rowsUpdated > 0) {
                noOfTableUpdated++;
            }

            psUpdateQualityCheck.setLong(1, productId);
            //Later if we need to add the info, we can add it here
            psUpdateQualityCheck.setString(2, "");
            psUpdateQualityCheck.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            psUpdateQualityCheck.setObject(4, userId);
            rowsUpdated = psUpdateQualityCheck.executeUpdate();
            psProduct.setLong(1, productId);
            rowsUpdated += psProduct.executeUpdate();
            if (rowsUpdated > 0) {
                noOfTableUpdated++;
            }
        } catch (SQLException _) {
            throw new BelmanException("Error signing quality document");
        }

        return noOfTableUpdated >= 1;
    }

    public boolean isDocumentExists(String orderNumber) {
        String sql = "SELECT COUNT(*) FROM dbo.QualityCheckDoc WHERE product_id = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            long productId = getProductIdFromProductNumber(orderNumber);
            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException _) {
            throw new BelmanException("Error checking if document exists");
        }
        return false;
    }

    public void savePdfToDb(String productNo, ByteArrayOutputStream outputStream, UUID userId) {
        long productId = getProductIdFromProductNumber(productNo);
        String sql = "INSERT INTO dbo.QualityCheckDoc (generated_by, product_id, generated_at, document) " +
                     "VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            ps.setLong(2, productId);
            ps.setBytes(3, outputStream.toByteArray());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving PDF to DB: {}", e.getMessage());
        }
    }

    public void sendBackToOperator(String orderNumber, UUID userId, String angle) {
        long productId = getProductIdFromProductNumber(orderNumber);
        String sql = """
        UPDATE dbo.Photos
        SET is_deleted   = 1,
            deleted_by   = ?,
            deleted_at   = CURRENT_TIMESTAMP
        WHERE product_id = ? AND angle = ? AND is_deleted = 0 AND is_signed = 0
        """;
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setLong(  2, productId);
            ps.setString(3, angle);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new BelmanException("Error soft-deleting photos " + ex);
        }
    }

    public List<Photo> getPhotosByONum(String orderNumber) {
        long productId = getProductIdFromProductNumber(orderNumber);
        List<Photo> photos = new ArrayList<>();
        final String selectSql = "SELECT * FROM Photos WHERE product_id = ? AND is_deleted = 0";

        try (Connection con = connectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Photo p = new Photo(
                        rs.getLong( ID),
                        rs.getObject( UPLOADED_BY, UUID.class),
                        rs.getString( PHOTOS_ANGLE),
                        rs.getTimestamp("uploaded_at").toLocalDateTime(),
                        rs.getBoolean( IS_DELETED),
                        rs.getBytes("photo_file")
                );
                if (rs.getBoolean(IS_DELETED)) {
                    p.setDeletedBy(rs.getObject( DELETED_BY, UUID.class));
                    p.setDeletedAt(rs.getTimestamp( DELETED_AT) != null ? rs.getTimestamp( DELETED_AT).toLocalDateTime() : null);
                }
                photos.add(p);
            }
        } catch (SQLException e) {
            logger.error("Error fetching photos for order: {}", e.getMessage());
        }
        return photos;
    }

    public void savePhotosBinary(List<Photo> photos, String orderNumber) {
        String sql = """
        INSERT INTO Photos
          (product_id, angle, uploaded_by, uploaded_at, photo_file)
        VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            long productId = getProductIdFromProductNumber(orderNumber);
            if (productId <= 0) throw new BelmanException("Product not found");

            c.setAutoCommit(false);
            for (Photo p : photos) {
                ps.setLong(1, productId);
                ps.setString(2, p.getAngle());
                ps.setObject(3, p.getUploadedBy());
                ps.setTimestamp(4, Timestamp.valueOf(p.getUploadedAt()));
                ps.setBytes(5, p.getPhotoFile());
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
        } catch (SQLException ex) {
            throw new BelmanException("Error uploading photos " + ex);
        }
    }

    public int getPhotosNumbersforOrder(String orderNumberToSign) {
        long productId = getProductIdFromProductNumber(orderNumberToSign);
        String sql = "SELECT COUNT(*) FROM dbo.Photos WHERE product_id = ? AND is_deleted = 0";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error fetching photo count for order: {}", e.getMessage());
        }
        return 0;
    }

    public List<Order> getOrders(String username) {
        List<Order> orders = new ArrayList<>();
        String selectSql;

        if(username == null)
        {
            selectSql = "SELECT * FROM Products;";
        }
        else
        {
            selectSql = "SELECT Products.*  " +
                        "FROM Products  " +
                        "JOIN Users ON Products.operator_id = Users.id  " +
                        "WHERE Users.username = ?;";
        }

        try (Connection con = connectionManager.getConnection();
             PreparedStatement psSelect = con.prepareStatement(selectSql))
        {
            if(username != null)
            {
                psSelect.setString(1, username);
            }
            ResultSet rs = psSelect.executeQuery();

            while(rs.next())
            {
                Long id = rs.getLong(ID);
                String orderNumber = rs.getString("product_number");
                LocalDateTime createdAt = rs.getTimestamp( USER_CREATED_AT).toLocalDateTime();
                Boolean isDeleted = rs.getBoolean( IS_DELETED);
                Boolean isSigned = rs.getBoolean("is_signed");
                List<Photo> photos = getPhotos(id);

                Order o = new Order(id, orderNumber, createdAt, isDeleted, photos, isSigned);
                orders.add(o);
            }

            return orders;
        }
        catch (SQLException e)
        {
            logger.error("Error fetching the orders: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<Photo> getPhotos(Long orderId)
    {
        List<Photo> photos = new ArrayList<>();
        final String selectSql = "SELECT * FROM Photos JOIN Products ON Photos.product_id = Products.id WHERE Photos.product_id = ? AND Photos.is_deleted = 0";
        try(Connection con = connectionManager.getConnection();
            PreparedStatement psSelect = con.prepareStatement(selectSql))
        {
            psSelect.setObject(1, orderId);
            ResultSet rs = psSelect.executeQuery();
            while(rs.next())
            {
                Long id = rs.getLong( ID);
                UUID uploadedBy = rs.getObject( UPLOADED_BY, UUID.class);
                String angle = rs.getString( PHOTOS_ANGLE);
                LocalDateTime uploadedAt = rs.getTimestamp(UPLOADED_AT).toLocalDateTime();
                boolean isDeleted = rs.getBoolean( IS_DELETED);
                UUID deletedBy = rs.getObject( DELETED_BY, UUID.class);
                Timestamp deletedAtTS = rs.getTimestamp( DELETED_AT);
                LocalDateTime deletedAt = deletedAtTS != null ? deletedAtTS.toLocalDateTime() : null;
                byte[] data = rs.getBytes("photo_file");
                Photo p = new Photo(id, uploadedBy, angle, uploadedAt, isDeleted, data);
                if (isDeleted) {
                    p.setDeletedBy(deletedBy);
                    p.setDeletedAt(deletedAt);
                }
                photos.add(p);
            }
            return photos;
        }
        catch (SQLException e)
        {
            throw new BelmanException("Error fetching photos: " + e.getMessage());
        }
    }
}
