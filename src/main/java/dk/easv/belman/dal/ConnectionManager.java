package dk.easv.belman.dal;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import dk.easv.belman.exceptions.BelmanException;

import java.sql.Connection;

public class ConnectionManager
{
    private final SQLServerDataSource ds;

    public ConnectionManager() throws BelmanException
    {
        ds = new SQLServerDataSource();
        ds.setServerName("EASV-DB4");
        ds.setDatabaseName("EASV_Ticket_moet");
        ds.setPortNumber(1433);
        ds.setUser("CSe2024b_e_8");
        ds.setPassword("CSe2024bE8!24");
        ds.setTrustServerCertificate(true);
    }

    public Connection getConnection() throws SQLServerException
    {
        return ds.getConnection();
    }
}
