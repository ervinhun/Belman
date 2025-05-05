package dk.easv.belman.dal;

import dk.easv.belman.be.QualityDocument;
import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.exceptions.BelmanException;
import dk.easv.belman.be.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DALManager {

    private final ConnectionManager connectionManager;

    public DALManager() throws BelmanException {
        connectionManager = new ConnectionManager();
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = """
                SELECT  u.id,
                        u.full_name,
                        u.username,
                        u.password,
                        u.tag_id,
                        u.role_id,
                        u.created_at,
                        u.last_login_time,
                        u.is_active,
                        r.role
                FROM    dbo.Users u
                JOIN    dbo.Roles r ON u.role_id = r.id
                WHERE   u.is_active = 1
                """;
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UUID id = rs.getObject("id", UUID.class);
                String fullName = rs.getString("full_name");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String tagId = rs.getString("tag_id");
                int roleId = rs.getInt("role_id");
                String roleName = rs.getString("role");

                LocalDateTime created = rs.getTimestamp("created_at").toLocalDateTime();
                Timestamp lastTS = rs.getTimestamp("last_login_time");
                LocalDateTime lastLogin = lastTS != null ? lastTS.toLocalDateTime() : null;

                boolean active = rs.getBoolean("is_active");

                User u = new User(id, fullName, username, password, tagId, roleId, created, lastLogin, active);
                u.setRole(roleName);
                list.add(u);
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching users from DB", ex);
        }
    }

    public UUID insertUser(User u) {
        String sql = """
                INSERT INTO dbo.Users
                    (id, full_name, username, password, tag_id,
                     role_id, created_at, last_login_time, is_active)
                OUTPUT INSERTED.ID
                VALUES
                    (NEWID(),?,?,?,?,?,?,?,1)
                """;
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            if (u.getTagId() == null)
                ps.setNull(4, Types.VARCHAR);
            else
                ps.setString(4, u.getTagId());
            ps.setInt(5, u.getRoleId());

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setTimestamp(6, now);
            ps.setTimestamp(7, now);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID id = rs.getObject(1, UUID.class);
                    u.setId(id);
                    return id;
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Error inserting user", ex);
        }
    }

    public boolean updateUser(User u) {
        String sql = """
                UPDATE dbo.Users
                SET full_name      = ?,
                    username       = ?,
                    password       = ?,
                    tag_id         = ?,
                    role_id        = ?,
                    last_login_time= ?
                WHERE id = ? AND is_active = 1
                """;
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            if (u.getTagId() == null)
                ps.setNull(4, Types.VARCHAR);
            else
                ps.setString(4, u.getTagId());
            ps.setInt(5, u.getRoleId());
            if (u.getLastLoginTime() == null)
                ps.setNull(6, Types.TIMESTAMP);
            else
                ps.setTimestamp(6, Timestamp.valueOf(u.getLastLoginTime()));
            ps.setObject(7, u.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Error updating user", ex);
        }
    }

    public void deleteUser(UUID id) {
        String sql = "UPDATE dbo.Users SET is_active = 0 WHERE id = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error deleting user", ex);
        }
    }

    public List<Order> getOrders(String username)
    {
        List<Order> orders = new ArrayList<>();
        String selectSql;

        if(username == null)
        {
            selectSql = "SELECT * FROM Products;";
        }
        else
        {
            // ADD LOGIC
            selectSql = "SELECT * FROM Products WHERE is_deleted = 0;";
        }

        try (Connection con = connectionManager.getConnection();
             PreparedStatement psSelect = con.prepareStatement(selectSql);
             ResultSet rs = psSelect.executeQuery())
        {

            while(rs.next())
            {
                Long id = rs.getLong("id");
                String orderNumber = rs.getString("product_number");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();;
                Boolean isDeleted = rs.getBoolean("is_deleted");
                //ADD IS SIGNED TO ORDERS IN DB
                //Boolean isSigned = rs.getBoolean("is_signed");
                List<Photo> photos = getPhotos(id);

                Order o = new Order(id, orderNumber, createdAt, isDeleted, photos, false);
                orders.add(o);
            }

            return orders;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error fetching the orders: "+e.getMessage());
        }
    }

    private List<Photo> getPhotos(Long orderId)
    {
        List<Photo> photos = new ArrayList<>();
        final String selectSql = "SELECT * FROM Photos, Products WHERE Photos.product_id = ?";

        try(Connection con = connectionManager.getConnection();
            PreparedStatement psSelect = con.prepareStatement(selectSql))
        {
            psSelect.setObject(1, orderId);
            ResultSet rs = psSelect.executeQuery();

            while(rs.next())
            {
                Long id = rs.getLong("id");
                UUID uploadedBy = rs.getObject("uploaded_by", UUID.class);
                String imagePath = rs.getString("image_path");
                LocalDateTime uploadedAt = rs.getTimestamp("created_at").toLocalDateTime();
                Boolean isDeleted = rs.getBoolean("is_deleted");
                UUID deletedBy = rs.getObject("deleted_by", UUID.class);
                Timestamp deletedAtTS = rs.getTimestamp("deleted_at");
                LocalDateTime deletedAt = deletedAtTS != null ? deletedAtTS.toLocalDateTime() : null;

                Photo p = new Photo(id, uploadedBy, imagePath, uploadedAt, isDeleted, deletedBy, deletedAt);
                photos.add(p);
            }

            return photos;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error fetching photos: "+e.getMessage());
        }
    }

    public User login(String username, String hashedPassword) {
        final String selectSql =
                "SELECT id, full_name, username, password, tag_id, role_id, created_at, last_login_time, is_active " +
                        "FROM Users WHERE username = ? AND password = ?";
        final String updateSql =
                "UPDATE Users SET last_login_time = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection con = connectionManager.getConnection();
             PreparedStatement psSelect = con.prepareStatement(selectSql)) {

            psSelect.setString(1, username);
            psSelect.setString(2, hashedPassword);

            try (ResultSet rs = psSelect.executeQuery()) {
                if (!rs.next()) return null;

                String idStr = rs.getString("id");
                UUID id = UUID.fromString(idStr);

                String fullName        = rs.getString("full_name");
                String user            = rs.getString("username");
                String storedHash      = rs.getString("password");
                String tagId           = rs.getString("tag_id");
                int    roleId          = rs.getInt("role_id");
                LocalDateTime createdAt       = rs.getTimestamp("created_at").toLocalDateTime();
                boolean active       = rs.getBoolean("is_active");

                // update last_login_time
                try (PreparedStatement psUpdate = con.prepareStatement(updateSql)) {
                    psUpdate.setString(1, id.toString());
                    psUpdate.executeUpdate();
                }
                LocalDateTime newLastLogin = LocalDateTime.now();

                return new User(
                        id,
                        fullName,
                        user,
                        storedHash,
                        tagId,
                        roleId,
                        createdAt,
                        newLastLogin,
                        active
                );
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Error logging in user", ex);
        }
    }

    public QualityDocument signOrder(String productNumber, UUID userId, String info, String qDocPath) {
        String sql = "INSERT INTO dbo.QualityChecks (product_id, info, signed_by, signed_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        Long newId = (long) -1;
        long productId = getProductIdFromProductNumber(productNumber);
        if (productId == -1) {
            throw new BelmanException("Product not found");
        }
        QualityDocument qDoc = new QualityDocument(userId, productId);

        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, productNumber);
            ps.setString(2, info);
            ps.setObject(3, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                newId = rs.getLong(1);
                qDoc.setId(newId);
                qDoc.setProductId(productId);
                qDoc.setGeneratedAt(rs.getTimestamp("signed_at").toLocalDateTime());
                qDoc.setQcDocPath(qDocPath);
            }
            return qDoc;
        } catch (SQLException _) {
            throw new BelmanException("Error signing order");
        }
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

    public boolean signQualityDocument(QualityDocument qcDoc) {
        int noOfTableUpdated = 0;
        String sqlPhotos = "UPDATE dbo.Photos SET is_signed = 1 WHERE product_id = ?";
        String sqlQualityDocument = "INSERT INTO dbo.QualityCheckDoc (generated_by, product_id, generated_at, qc_doc_path) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection c = connectionManager.getConnection();
                PreparedStatement psPhotos = c.prepareStatement(sqlPhotos);
                PreparedStatement psQualityDocument = c.prepareStatement(sqlQualityDocument)) {

                psPhotos.setLong(1, qcDoc.getProductId());
                int rowsUpdated = psPhotos.executeUpdate();
                if (rowsUpdated > 0) {
                    noOfTableUpdated++;
                }

                psQualityDocument.setObject(1, qcDoc.getGeneratedBy());
                psQualityDocument.setLong(2, qcDoc.getProductId());
                psQualityDocument.setString(3, qcDoc.getQcDocPath());
                rowsUpdated = psQualityDocument.executeUpdate();
                if (rowsUpdated > 0) {
                    noOfTableUpdated++;
                }
            } catch (SQLException _) {
                throw new BelmanException("Error signing quality document");
            }

        return noOfTableUpdated == 2;
    }

    public boolean checkIfDocumentExists(String orderNumber) {
        long productId = getProductIdFromProductNumber(orderNumber);
        String sql = "SELECT COUNT(*) FROM dbo.QualityCheckDoc WHERE product_id = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

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

    public String getDocumentPath(String orderNumber) {
        long productId = getProductIdFromProductNumber(orderNumber);
        String sql = "SELECT qc_doc_path FROM dbo.QualityCheckDoc WHERE product_id = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("qc_doc_path");
            }
        } catch (SQLException _) {
            throw new BelmanException("Error fetching document path");
        }
        return null;
    }

    public void uploadPhotos(String orderNumber, ArrayList<String> fileNames, UUID userId) {
        String sql = "INSERT INTO dbo.Photos (product_id, image_path, uploaded_by, uploaded_at) " +
                "VALUES (?, ?, ?,  CURRENT_TIMESTAMP)";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            long productId = getProductIdFromProductNumber(orderNumber);
            if (productId == -1) {
                throw new BelmanException("Product not found");
            }
            c.setAutoCommit(false);
            for (String fileName : fileNames) {
                String filePath = "SavedImages/" + orderNumber + "/" + fileName;
                ps.setLong(1, productId);
                ps.setString(2, filePath);
                ps.setObject(3, userId);
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
            c.setAutoCommit(true);
        } catch (SQLException _) {
            throw new BelmanException("Error uploading photos");
        }
    }
}
