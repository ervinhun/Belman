package dk.easv.belman.dal;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import dk.easv.belman.config.ConfigCrypto;
import dk.easv.belman.exceptions.BelmanException;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

public class ConnectionManager {
    private final SQLServerDataSource ds;

    public ConnectionManager() throws BelmanException
    {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new BelmanException("I/O exception: " + e);
        }


        try {
            String dbTrustServerCertificate = ConfigCrypto.decrypt(props.getProperty("db.trust_server_certificate"));
            String dbUser = ConfigCrypto.decrypt(props.getProperty("db.user"));
            String dbPassword = ConfigCrypto.decrypt(props.getProperty("db.password"));
            String dbServerName = ConfigCrypto.decrypt(props.getProperty("db.server_name"));
            String dbDatabaseName = ConfigCrypto.decrypt(props.getProperty("db.database_name"));
            String dbPortNumber = ConfigCrypto.decrypt(props.getProperty("db.port_number"));

            ds = new SQLServerDataSource();
            ds.setServerName(dbServerName);
            ds.setDatabaseName(dbDatabaseName);
            ds.setPortNumber(Integer.parseInt(dbPortNumber));
            ds.setUser(dbUser);
            ds.setPassword(dbPassword);
            ds.setTrustServerCertificate(Boolean.parseBoolean(dbTrustServerCertificate));
        } catch (Exception e) {
            throw new BelmanException("Error connecting to the db - " + e);
        }

    }

    public Connection getConnection() throws SQLServerException {
        return ds.getConnection();
    }
}
