package dk.easv.belman.dal;

import dk.easv.belman.be.Order;
import dk.easv.belman.be.Photo;
import dk.easv.belman.be.QualityDocument;
import dk.easv.belman.exceptions.BelmanException;
import dk.easv.belman.be.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DALManager {

    private final ConnectionManager connectionManager;
    private static final Logger logger = LoggerFactory.getLogger(DALManager.class);
    
    private static final String USER_LAST_LOGIN_TIME = "last_login_time";
    private static final String ID =  "id";
    private static final String PHOTOS_IMAGE_PATH = "image_path";
    private static final String USER_FULL_NAME =  "full_name";
    private static final String USER_USERNAME =  "username";
    private static final String USER_PASSWORD =  "password";
    private static final String USER_TAG_ID =  "tag_id";
    private static final String USER_ROLE_ID =  "role_id";
    private static final String USER_IS_ACTIVE =   "is_active";
    private static final String USER_CREATED_AT =   "created_at";
    private static final String PHOTOS_ANGLE =   "angle";
    private static final String IS_DELETED =    "is_deleted";
    private static final String DELETED_BY =    "deleted_by";
    private static final String DELETED_AT =    "deleted_at";
    private static final String UPLOADED_BY =  "uploaded_by";


    public DALManager() throws BelmanException {
        connectionManager = new ConnectionManager();
    }

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
                UUID id = rs.getObject( ID, UUID.class);
                String fullName = rs.getString( USER_FULL_NAME);
                String username = rs.getString( USER_USERNAME);
                String password = rs.getString( USER_PASSWORD);
                String tagId = rs.getString( USER_TAG_ID);
                int roleId = rs.getInt( USER_ROLE_ID);
                String roleName = rs.getString("role");

                LocalDateTime created = rs.getTimestamp( USER_CREATED_AT).toLocalDateTime();
                Timestamp lastTS = rs.getTimestamp(USER_LAST_LOGIN_TIME);
                LocalDateTime lastLogin = lastTS != null ? lastTS.toLocalDateTime() : null;

                boolean active = rs.getBoolean( USER_IS_ACTIVE);

                User u = new User(id, fullName, username, password, tagId, roleId, active);
                u.setLastLoginTime(lastLogin);
                u.setCreatedAt(created);
                u.setRole(roleName);
                list.add(u);
            }
            return list;
        } catch (SQLException ex) {
            logger.error("Error fetching users from DB", ex);
        }
        return Collections.emptyList();
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
            logger.error("Error inserting user", ex);
        }
        return null;
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
            logger.error("Error updating user", ex);
        }
        return false;
    }

    public void deleteUser(UUID id) {
        String sql = "UPDATE dbo.Users SET is_active = 0 WHERE id = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new BelmanException("Error deleting user " + ex);
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
             PreparedStatement psQualityDocument = c.prepareStatement(sqlQualityDocument);
             PreparedStatement psUpdateQualityCheck = c.prepareStatement(sqlUpdateQuailityCheck)) {

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

            psUpdateQualityCheck.setLong(1, qcDoc.getProductId());
            //Later if we need to add the info, we can add it here
            psUpdateQualityCheck.setString(2, "");
            psUpdateQualityCheck.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            psUpdateQualityCheck.setObject(4, qcDoc.getGeneratedBy());
            rowsUpdated = psUpdateQualityCheck.executeUpdate();
            if (rowsUpdated > 0) {
                noOfTableUpdated++;
            }
        } catch (SQLException _) {
            throw new BelmanException("Error signing quality document");
        }

        return noOfTableUpdated >= 2;
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
                Long id = rs.getLong( ID);
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
        final String selectSql = "SELECT * FROM Photos, Products WHERE Photos.product_id = ? AND Photos.is_deleted = 0";

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
                LocalDateTime uploadedAt = rs.getTimestamp( USER_CREATED_AT).toLocalDateTime();
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

    //photos with angle
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


    public void savePhotos(List<Photo> photos, String orderNumber) {
        String sql = "INSERT INTO dbo.Photos (product_id, image_path, angle, uploaded_by, uploaded_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            long productId = getProductIdFromProductNumber(orderNumber);
            if (productId == -1) {
                throw new BelmanException("Product not found");
            }
            Timestamp uploadedAt = Timestamp.valueOf(photos.get(0).getUploadedAt());
            ps.setLong(1, productId);
            ps.setObject(5, uploadedAt);
            
            c.setAutoCommit(false);
            for (Photo photo : photos) {
                ps.setString(2, photo.getImagePath());
                ps.setString(3, photo.getAngle());
                ps.setObject(4, photo.getUploadedBy());
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
            c.setAutoCommit(true);
        } catch (SQLException _) {
            throw new BelmanException("Error uploading photos");
        }
    }

    private void insertLoginLog(Connection conn, UUID userId, String method) throws SQLException {
        String sql = """
        INSERT INTO LoginLogs (user_id, login_time, method)
        VALUES (?, CURRENT_TIMESTAMP, ?)
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setString(2, method);
            ps.executeUpdate();
        }
    }

    public User login(String username, String hashedPassword) {
        final String selectSql =
                "SELECT id, full_name, username, password, tag_id, role_id, created_at, last_login_time, is_active " +
                        "FROM Users WHERE username = ? AND password = ?";
        final String updateSql =
                "UPDATE Users SET last_login_time = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection con = connectionManager.getConnection()) {
            con.setAutoCommit(false);

            UUID id;
            User user;
            try (PreparedStatement psSelect = con.prepareStatement(selectSql)) {
                psSelect.setString(1, username);
                psSelect.setString(2, hashedPassword);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return null;
                    }
                    id = UUID.fromString(rs.getString( ID));
                    user = new User(
                            id,
                            rs.getString( USER_FULL_NAME),
                            rs.getString( USER_USERNAME),
                            rs.getString( USER_PASSWORD),
                            rs.getString( USER_TAG_ID),
                            rs.getInt( USER_ROLE_ID),
                            rs.getBoolean( USER_IS_ACTIVE)
                    );
                    user.setLastLoginTime(null);
                    user.setCreatedAt(rs.getTimestamp( USER_CREATED_AT).toLocalDateTime());
                }
            }

            try (PreparedStatement psUpdate = con.prepareStatement(updateSql)) {
                psUpdate.setObject(1, id);
                psUpdate.executeUpdate();
            }

            insertLoginLog(con, id, "login");

            con.commit();

            user.setLastLoginTime(java.time.LocalDateTime.now());
            return user;

        } catch (SQLException ex) {
            throw new BelmanException("Error logging in user " + ex);
        }
    }

    public void logout(UUID userId) {
        try (Connection con = connectionManager.getConnection()) {
            insertLoginLog(con, userId, "logout");
        } catch (SQLException ex) {
            throw new BelmanException("Error logging out user " + ex);
        }
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

    public User getUserById(UUID userId) {
        String sql = "SELECT * FROM Users WHERE id = ? AND is_active = 1";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getObject( ID, UUID.class),
                        rs.getString( USER_FULL_NAME),
                        rs.getString( USER_USERNAME),
                        rs.getString( USER_PASSWORD),
                        rs.getString( USER_TAG_ID),
                        rs.getInt( USER_ROLE_ID),
                        rs.getBoolean( USER_IS_ACTIVE)
                );
                user.setCreatedAt(rs.getTimestamp( USER_CREATED_AT).toLocalDateTime());
                user.setLastLoginTime(rs.getTimestamp(USER_LAST_LOGIN_TIME) != null ? rs.getTimestamp(USER_LAST_LOGIN_TIME).toLocalDateTime() : null);
                return user;
            }
        } catch (SQLException e) {
            logger.error("Error fetching user by ID: {}", e.getMessage());
        }
        return null;
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

    public void savePdfToDb(String productNo, ByteArrayOutputStream outputStream, UUID userId) {
        Long productId = getProductIdFromProductNumber(productNo);
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
    public void sendBackToOperator(String orderNumber, UUID userId) {
        long productId = getProductIdFromProductNumber(orderNumber);
        String sql = """
        UPDATE dbo.Photos
        SET is_deleted   = 1,
            deleted_by   = ?,
            deleted_at   = CURRENT_TIMESTAMP
        WHERE product_id = ?
        """;
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setLong(  2, productId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new BelmanException("Error soft-deleting photos " + ex);
        }
    }
}