package com.matheusfig90.exceptions;

public class UnavailableDeviceException extends Exception {
    public UnavailableDeviceException(Long deviceId) {
        super(String.format("The device is not available (ID: %s)", deviceId));
    }

    public UnavailableDeviceException(String message) {
        super(message);
    }
}
