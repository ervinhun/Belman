package dk.easv.belman.dal;

import dk.easv.belman.be.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class DALManager {
    private final ConnectionManager connectionManager;

    public DALManager() {
        this.connectionManager = new ConnectionManager();
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
                boolean isActive       = rs.getBoolean("is_active");

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
                        isActive
                );
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Error logging in user", ex);
        }
    }

}
