package no.torand.surfsentry.service;

import no.torand.surfsentry.domain.AccessRule;
import no.torand.surfsentry.domain.Device;
import no.torand.surfsentry.domain.TimeInterval;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class WebAccessManager {
    private Map<String, Device> devicesByAddress;
    private Map<String, List<AccessRule>> allowedAccessByPerson;
    private Map<String, List<AccessRule>> deniedAccessByPerson;

    @PostConstruct
    public void init() {
        Properties properties = new Properties();
        try (InputStream stream = WebAccessManager.class.getResourceAsStream("/surfsentry.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String devicesProperty = properties.getProperty("devices", "");
        devicesByAddress = decodeDevicesProperty(devicesProperty).stream()
            .collect(toMap(Device::getAddress, d -> d));

        String allowedAccessProperty = properties.getProperty("allowAccess", "");
        allowedAccessByPerson = decodeAccessRuleProperty(AccessRule.RuleType.ALLOW, allowedAccessProperty).stream()
            .collect(Collectors.groupingBy(AccessRule::getPersonName));

        String deniedAccessProperty = properties.getProperty("denyAccess", "");
        deniedAccessByPerson = decodeAccessRuleProperty(AccessRule.RuleType.DENY, deniedAccessProperty).stream()
            .collect(Collectors.groupingBy(AccessRule::getPersonName));
    }

    public Optional<Device> getDevice(String clientAddress) {
        return Optional.ofNullable(devicesByAddress.get(clientAddress));
    }

    public boolean isAccessAllowed(String clientAddress, String remoteUri) {
        Device device = devicesByAddress.get(clientAddress);
        if (nonNull(device)) {
            return isAllowedAccess(device.getOwner(), remoteUri) && !isDeniedAccess(device.getOwner(), remoteUri);
        }

        return false;
    }

    private boolean isAllowedAccess(String personName, String remoteUri) {
        return matchingRuleExist(personName, remoteUri, allowedAccessByPerson);
    }

    private boolean isDeniedAccess(String personName, String remoteUri) {
        return matchingRuleExist(personName, remoteUri, deniedAccessByPerson);
    }

    private boolean matchingRuleExist(String personName, String remoteUri, Map<String, List<AccessRule>> rulesByPerson) {
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
}
