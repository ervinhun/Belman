package dk.easv.belman.config;

import java.io.FileInputStream;
import java.util.Properties;

import static dk.easv.belman.dal.FilePaths.CONFIG_PATH;

public class ConfigReader {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            props.load(in);
        }
    }
}

