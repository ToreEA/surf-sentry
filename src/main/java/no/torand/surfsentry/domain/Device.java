package no.torand.surfsentry.domain;


import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class Device {
    private final String owner;
    private final String type;
    private final String address;

    public Device(String owner, String type, String address) {
        this.owner = owner;
        this.type = type;
        this.address = requireNonNull(address, "address not specified");
    }

    public String getOwner() {
        return owner;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getDisplayName() {
        if (isNull(owner) || isNull(type)) {
            return "Unknown device (" + address + ")";
        } else {
            return owner + "'s " + type + " (" + address + ")";
        }
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public static Device unknown(String address) {
        return new Device(null, null, address);
    }
}
