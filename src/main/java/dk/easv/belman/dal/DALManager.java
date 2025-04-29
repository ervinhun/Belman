package dk.easv.belman.dal;

import dk.easv.belman.be.User;
import dk.easv.belman.exceptions.BelmanException;

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
            SELECT u.*, r.role
            FROM dbo.Users u
            JOIN dbo.Roles r ON u.role_id = r.id
            WHERE u.is_active = 1
            """;
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID id = rs.getObject("id", UUID.class);
                String fullName = rs.getString("full_name");
                String username = rs.getString("username");
                String password = rs.getString("password");
                Integer tag = rs.getObject("tag_id") == null ? null : rs.getInt("tag_id");
                int roleId = rs.getInt("role_id");
                String roleName = rs.getString("role");
                LocalDateTime created = rs.getTimestamp("created_at").toLocalDateTime();
                Timestamp ts = rs.getTimestamp("last_login_time");
                LocalDateTime last = ts == null ? null : ts.toLocalDateTime();
                boolean active = rs.getBoolean("is_active");
                User u = new User(id, fullName, username, password, tag, roleId, created, last, active);
                u.setRole(roleName);
                list.add(u);
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public UUID insertUser(User u) {
        String sql = """
    INSERT INTO dbo.Users
    (id, full_name, username, password, tag_id, role_id, created_at, is_active)
    OUTPUT INSERTED.ID
    VALUES (NEWID(),?,?,?,?,?,SYSDATETIME(),1)
    """;
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            if (u.getTagId() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, u.getTagId());
            ps.setInt(5, u.getRoleId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID id = rs.getObject(1, UUID.class);
                    u.setId(id);
                    return id;
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean updateUser(User u) {
        String sql = """
            UPDATE dbo.Users
            SET full_name=?, username=?, password=?, tag_id=?, role_id=?, last_login_time=?
            WHERE id=? AND is_active=1
            """;
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            if (u.getTagId() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, u.getTagId());
            ps.setInt(5, u.getRoleId());
            if (u.getLastLoginTime() == null) ps.setNull(6, Types.TIMESTAMP); else ps.setTimestamp(6, Timestamp.valueOf(u.getLastLoginTime()));
            ps.setObject(7, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void deleteUser(UUID id) {
        String sql = "UPDATE dbo.Users SET is_active = 0 WHERE id = ?";
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
