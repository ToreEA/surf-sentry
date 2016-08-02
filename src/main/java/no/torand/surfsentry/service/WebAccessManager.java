package no.torand.surfsentry.service;

import no.torand.surfsentry.domain.AccessRule;
import no.torand.surfsentry.domain.Device;
import no.torand.surfsentry.domain.TimeInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.*;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class WebAccessManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebAccessManager.class);

    private static final String SYSTEMPROPERTY_CONFIGDIR = "surfsentry.configdir";
    private static final String CONFIG_FILENAME = "surfsentry.properties";

    private Map<String, Device> devicesByAddress;
    private Map<String, List<AccessRule>> allowedAccessByPerson;
    private Map<String, List<AccessRule>> deniedAccessByPerson;

    @PostConstruct
    public void init() {
        File configFile = getConfigFile();

        if (nonNull(configFile)) {
            LOGGER.info("Configuration file = {}", configFile);

            Properties properties = new Properties();
            try (InputStream stream = new FileInputStream(configFile)) {
                properties.load(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String devicesProperty = properties.getProperty("devices", "");
            devicesByAddress = decodeDevicesProperty(devicesProperty).stream()
                    .collect(toMap(Device::getAddress, d -> d));

            String allowedAccessProperty = properties.getProperty("allowAccess", "");
            allowedAccessByPerson = decodeAccessRuleProperty(AccessRule.RuleType.ALLOW, allowedAccessProperty).stream()
                    .collect(groupingBy(AccessRule::getPersonName));

            String deniedAccessProperty = properties.getProperty("denyAccess", "");
            deniedAccessByPerson = decodeAccessRuleProperty(AccessRule.RuleType.DENY, deniedAccessProperty).stream()
                    .collect(groupingBy(AccessRule::getPersonName));
        } else {
            devicesByAddress = emptyMap();
            allowedAccessByPerson = emptyMap();
            deniedAccessByPerson = emptyMap();
        }
    }

    public Optional<Device> getDevice(String clientAddress) {
        return Optional.ofNullable(devicesByAddress.get(clientAddress));
    }

    public boolean isAccessAllowed(String clientAddress, String remoteUri) {
        Device device = devicesByAddress.get(clientAddress);
        return nonNull(device) &&
                isAllowedAccess(device.getOwner(), remoteUri) &&
                !isDeniedAccess(device.getOwner(), remoteUri);
    }

    private boolean isAllowedAccess(String personName, String remoteUri) {
        return matchingRuleExists(personName, remoteUri, allowedAccessByPerson);
    }

    private boolean isDeniedAccess(String personName, String remoteUri) {
        return matchingRuleExists(personName, remoteUri, deniedAccessByPerson);
    }

    private boolean matchingRuleExists(String personName, String remoteUri, Map<String, List<AccessRule>> rulesByPerson) {
        List<AccessRule> rules = new LinkedList<>();
        rules.addAll(rulesByPerson.getOrDefault(personName, emptyList()));
        rules.addAll(rulesByPerson.getOrDefault(AccessRule.WILDCARD, emptyList()));
        return rules.stream().anyMatch(r -> r.matches(remoteUri));
    }

    private List<Device> decodeDevicesProperty(String devicesProperty) {
        List<Device> decoded = new LinkedList<>();

        String[] deviceDefs = devicesProperty.split(",");
        for (String deviceDef : deviceDefs) {
            String[] fields = deviceDef.split("\\|");
            if (fields.length != 3) {
                throw new IllegalArgumentException("Missing fields for device: " + deviceDef);
            }

            Device device = new Device(fields[0], fields[1], fields[2]);
            decoded.add(device);
        }

        return decoded;
    }

    private List<AccessRule> decodeAccessRuleProperty(AccessRule.RuleType type, String propertyName) {
        List<AccessRule> decoded = new LinkedList<>();

        String[] ruleDefs = propertyName.split(",");
        for (String ruleDef : ruleDefs) {
            String[] fields = ruleDef.split("\\|");
            if (fields.length != 3) {
                throw new IllegalArgumentException("Missing fields for rule: " + ruleDef);
            }

            AccessRule rule = new AccessRule(type, fields[0], fields[1], TimeInterval.valueOf(fields[2]));
            decoded.add(rule);
        }

        return decoded;
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
