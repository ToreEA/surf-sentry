package no.torand.surfsentry.presentation.representation;

public class DeviceUsageJson {
    private String deviceName;
    private String lastRequestTime;

    public DeviceUsageJson(String deviceName, String lastRequestTime) {
        this.deviceName = deviceName;
        this.lastRequestTime = lastRequestTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getLastRequestTime() {
        return lastRequestTime;
    }
}
