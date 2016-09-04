package no.torand.surfsentry.service;

import no.torand.surfsentry.domain.AccessRule;
import no.torand.surfsentry.domain.Config;
import no.torand.surfsentry.domain.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.*;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@ApplicationScoped
public class WebAccessManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebAccessManager.class);

    private static final String SYSTEMPROPERTY_CONFIGDIR = "surfsentry.configdir";
    private static final String CONFIG_FILENAME = "surfsentry.properties";

    private Config config;

    @PostConstruct
    public void init() {
        File configFile = getConfigFile();

        if (nonNull(configFile)) {
            LOGGER.info("Configuration file = {}", configFile);

            try (InputStream stream = new FileInputStream(configFile)) {
                ConfigReader reader = new ConfigReader();
                config = reader.read(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            config = Config.empty();
        }
    }

    public Optional<Device> getDevice(String clientAddress) {
        return Optional.ofNullable(config.getDevicesByAddress().get(clientAddress));
    }

    public boolean isAccessAllowed(String clientAddress, String remoteUri) {
        Device device = config.getDevicesByAddress().get(clientAddress);
        return nonNull(device) &&
                isAllowedAccess(device.getOwner(), remoteUri) &&
                !isDeniedAccess(device.getOwner(), remoteUri);
    }

    private boolean isAllowedAccess(String personName, String remoteUri) {
        return matchingRuleExists(personName, remoteUri, config.getAllowedAccessByPerson());
    }

    private boolean isDeniedAccess(String personName, String remoteUri) {
        return matchingRuleExists(personName, remoteUri, config.getDeniedAccessByPerson());
    }

    private boolean matchingRuleExists(String personName, String remoteUri, Map<String, List<AccessRule>> rulesByPerson) {
        List<AccessRule> rules = new LinkedList<>();
        rules.addAll(rulesByPerson.getOrDefault(personName, emptyList()));
        rules.addAll(rulesByPerson.getOrDefault(AccessRule.WILDCARD, emptyList()));
        return rules.stream().anyMatch(r -> r.matches(remoteUri));
    }

    private File getConfigFile() {
        String configDir = System.getProperty(SYSTEMPROPERTY_CONFIGDIR);
        if (isNull(configDir) || configDir.isEmpty()) {
            LOGGER.error("System property {} not set", SYSTEMPROPERTY_CONFIGDIR);
            return null;
        }

        File configFile = new File(configDir, CONFIG_FILENAME);
        if (!configFile.exists()) {
            LOGGER.error("Configuration file {} not found", configFile.toString());
            return null;
        }

        return configFile;
    }
}
