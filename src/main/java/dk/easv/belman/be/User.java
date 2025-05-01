package dk.easv.belman.be;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String fullName;
    private String username;
    private String password;
    private String tagId;
    private int roleId;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginTime;
    private boolean active;

    public User() {}

    public User(UUID id, String fullName, String username, String password,
                String tagId, int roleId, LocalDateTime createdAt,
                LocalDateTime lastLoginTime, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.tagId = tagId;
        this.roleId = roleId;
        this.createdAt = createdAt;
        this.lastLoginTime = lastLoginTime;
        this.active = active;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
