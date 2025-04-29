package dk.easv.belman.dal;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import dk.easv.belman.exceptions.BelmanException;

import java.sql.Connection;

public class ConnectionManagerLocal {
    private final SQLServerDataSource ds;

    public ConnectionManagerLocal() throws BelmanException
    {
        ds = new SQLServerDataSource();
        ds.setServerName("localhost");
        ds.setDatabaseName("master");
        ds.setPortNumber(1433);
        ds.setUser("SA");
        ds.setPassword("StrongPass123");
        ds.setTrustServerCertificate(true);
    }

    public Connection getConnection() throws SQLServerException {
        return ds.getConnection();
    }
}
