package no.torand.surfsentry.service;

import no.torand.surfsentry.domain.AccessRule;
import no.torand.surfsentry.domain.Config;
import no.torand.surfsentry.domain.Device;
import no.torand.surfsentry.domain.TimeInterval;

import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class ConfigReader {

    public Config read(InputStream configStream) throws IOException {
        Properties properties = new Properties();
        properties.load(configStream);

        String devicesProperty = properties.getProperty("devices", "");
        Map<String, Device> devicesByAddress = decodeDevicesProperty(devicesProperty).stream()
                .collect(toMap(Device::getAddress, d -> d));

        String allowedAccessProperty = properties.getProperty("allowAccess", "");
        Map<String, List<AccessRule>> allowedAccessByPerson = decodeAccessRuleProperty(AccessRule.RuleType.ALLOW, allowedAccessProperty).stream()
                .collect(groupingBy(AccessRule::getPersonName));

        String deniedAccessProperty = properties.getProperty("denyAccess", "");
        Map<String, List<AccessRule>> deniedAccessByPerson = decodeAccessRuleProperty(AccessRule.RuleType.DENY, deniedAccessProperty).stream()
                .collect(groupingBy(AccessRule::getPersonName));

        return new Config(devicesByAddress, allowedAccessByPerson, deniedAccessByPerson);
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
            if (fields.length != 4) {
                throw new IllegalArgumentException("Missing fields for rule: " + ruleDef);
            }

            EnumSet<DayOfWeek> daysOfWeek = decodeDaysOfWeek(fields[2]);
            AccessRule rule = new AccessRule(type, fields[0], fields[1], daysOfWeek, TimeInterval.valueOf(fields[3]));
            decoded.add(rule);
        }

        return decoded;
    }

    private EnumSet<DayOfWeek> decodeDaysOfWeek(String daysOfWeekString) {
        EnumSet<DayOfWeek> daysOfWeek = EnumSet.allOf(DayOfWeek.class);

        if (!"*".equals(daysOfWeekString)) {
            if (daysOfWeekString.length() != 7 ) {
                throw new IllegalArgumentException("Invalid formatting of 'days of week': " + daysOfWeekString);
            }

            for (int dayNo = 1; dayNo <= 7; dayNo++) {
                if ("0Nn-".indexOf(daysOfWeekString.charAt(dayNo-1)) > -1) {
                    daysOfWeek.remove(DayOfWeek.of(dayNo));
                }
            }
        }

        return daysOfWeek;
    }
}
