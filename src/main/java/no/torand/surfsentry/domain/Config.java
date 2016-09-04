package no.torand.surfsentry.domain;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class Config {
    private final Map<String, Device> devicesByAddress;
    private final Map<String, List<AccessRule>> allowedAccessByPerson;
    private final Map<String, List<AccessRule>> deniedAccessByPerson;

    public static Config empty() {
        return new Config(emptyMap(), emptyMap(), emptyMap());
    }

    public Config(Map<String, Device> devicesByAddress, Map<String, List<AccessRule>> allowedAccessByPerson, Map<String, List<AccessRule>> deniedAccessByPerson) {
        this.devicesByAddress = devicesByAddress;
        this.allowedAccessByPerson = allowedAccessByPerson;
        this.deniedAccessByPerson = deniedAccessByPerson;
    }

    public Map<String, Device> getDevicesByAddress() {
        return devicesByAddress;
    }

    public Map<String, List<AccessRule>> getAllowedAccessByPerson() {
        return allowedAccessByPerson;
    }

    public Map<String, List<AccessRule>> getDeniedAccessByPerson() {
        return deniedAccessByPerson;
    }
}
