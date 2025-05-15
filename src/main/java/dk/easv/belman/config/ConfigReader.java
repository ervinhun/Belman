package dk.easv.belman.config;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        }

        String dbUrl = ConfigCrypto.decrypt(props.getProperty("db.url"));
        String dbUser = ConfigCrypto.decrypt(props.getProperty("db.user"));
        String dbPassword = ConfigCrypto.decrypt(props.getProperty("db.password"));
    }
}

