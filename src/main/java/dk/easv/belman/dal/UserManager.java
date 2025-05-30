package dk.easv.belman.dal;

import dk.easv.belman.be.User;
import dk.easv.belman.exceptions.BelmanException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UserManager extends DALManagerBase
{
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
                UUID id = rs.getObject(ID, UUID.class);
                String fullName = rs.getString(USER_FULL_NAME);
                String username = rs.getString(USER_USERNAME);
                String password = rs.getString(USER_PASSWORD);
                String tagId = rs.getString(USER_TAG_ID);
                int roleId = rs.getInt(USER_ROLE_ID);
                String roleName = rs.getString("role");

                LocalDateTime created = rs.getTimestamp( USER_CREATED_AT).toLocalDateTime();
                Timestamp lastTS = rs.getTimestamp(USER_LAST_LOGIN_TIME);
                LocalDateTime lastLogin = lastTS != null ? lastTS.toLocalDateTime() : null;

                boolean active = rs.getBoolean(USER_IS_ACTIVE);

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

    public User getUserById(UUID userId) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getObject(ID, UUID.class),
                        rs.getString(USER_FULL_NAME),
                        rs.getString(USER_USERNAME),
                        rs.getString(USER_PASSWORD),
                        rs.getString(USER_TAG_ID),
                        rs.getInt(USER_ROLE_ID),
                        rs.getBoolean(USER_IS_ACTIVE)
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

    public User login(String username, String hashedPassword) {
        String selectSql;
        if(username != null)
        {
            selectSql =
                    "SELECT id, full_name, username, password, tag_id, role_id, created_at, last_login_time, is_active " +
                    "FROM Users WHERE username = ? AND password = ?";
        }
        else
        {
            selectSql = "SELECT * FROM Users WHERE tag_id = ?";
        }

        final String updateSql =
                "UPDATE Users SET last_login_time = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection con = connectionManager.getConnection()) {
            con.setAutoCommit(false);

            UUID id;
            User user;
            try (PreparedStatement psSelect = con.prepareStatement(selectSql)) {
                if(username != null)
                {
                    psSelect.setString(1, username);
                    psSelect.setString(2, hashedPassword);
                }
                else
                {
                    psSelect.setString(1, hashedPassword);
                }

                try (ResultSet rs = psSelect.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return null;
                    }
                    id = UUID.fromString(rs.getString( ID));
                    user = new User(
                            id,
                            rs.getString(USER_FULL_NAME),
                            rs.getString(USER_USERNAME),
                            rs.getString(USER_PASSWORD),
                            rs.getString(USER_TAG_ID),
                            rs.getInt(USER_ROLE_ID),
                            rs.getBoolean(USER_IS_ACTIVE)
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
}
