package no.cantara.ratpack.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.func.Action;
import ratpack.server.BaseDir;
import ratpack.server.ServerConfigBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class RatpackConfigs {

    private static final Logger log = LoggerFactory.getLogger(RatpackConfigs.class);

    public static Action<ServerConfigBuilder> configuration(String applicationName, int httpPort, String defaultConfigurationResourcePath, String overrideConfigurationFilePath) {
        return serverConfigBuilder -> {
            log.info("loading default configuration from resource on classpath: " + defaultConfigurationResourcePath);
            serverConfigBuilder
                    .props(ImmutableMap.of("app.name", applicationName))
                    .port(httpPort)
                    .props(Resources.getResource(defaultConfigurationResourcePath)); // default config from classpath
            Path overridePath = Paths.get(overrideConfigurationFilePath);
            File overrideFile = overridePath.toFile();
            if (overrideFile.isFile() && overrideFile.canRead()) {
                log.info("loading override configuration from file: {}",  overrideFile.getCanonicalPath().toString());
                Properties properties = readProperties(overrideFile);
                serverConfigBuilder.props(properties); // override configuration from file
                String ratpackPortStr = properties.getProperty("ratpack.port");
                if (!Strings.isNullOrEmpty(ratpackPortStr)) {
                    try {
                        int httpOverridePort = Integer.parseInt(ratpackPortStr);
                        if (httpOverridePort >= 0 && httpOverridePort <= 65535) {
                            serverConfigBuilder.port(httpOverridePort);
                        } else {
                            log.info("Illegal port number found in override configuration: ratpack.port={}", ratpackPortStr);
                        }
                    } catch (NumberFormatException e) {
                        log.info("Unable to parse property found in override configuration: ratpack.port={}", ratpackPortStr);
                    }
                }
            } else {
                log.info("no override configuration file found at path: {}", overrideFile.getCanonicalPath().toString());
            }
            serverConfigBuilder
                    .env()
                    .sysProps()
                    .baseDir(BaseDir.find());
        };
    }

    private static Properties readProperties(File propertiesFile) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(propertiesFile), Charset.forName("UTF-8")))) {
            properties.load(br);
        }
        return properties;
    }
}
