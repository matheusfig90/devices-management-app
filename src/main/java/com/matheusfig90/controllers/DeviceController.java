package com.matheusfig90.controllers;

import com.matheusfig90.controllers.requests.BookDeviceRequestBody;
import com.matheusfig90.entity.Booking;
import com.matheusfig90.exceptions.EntityNotFoundException;
import com.matheusfig90.exceptions.UnavailableDeviceException;
import com.matheusfig90.service.DeviceService;
import com.matheusfig90.service.DeviceService.DeviceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("devices")
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    @GetMapping("/{id}")
    public DeviceInfo getDeviceById(@PathVariable("id") Long deviceId) {
        try {
            DeviceInfo deviceInfo = deviceService.getInfo(deviceId);

            return deviceInfo;
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{id}/book")
    public Booking bookDevice(@PathVariable("id") Long deviceId, @RequestBody BookDeviceRequestBody requestBody) {
        try {
            Booking booking = deviceService.bookDevice(deviceId, requestBody.userId());

            return booking;
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnavailableDeviceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}/return")
    public Booking returnDevice(@PathVariable("id") Long deviceId) {
        try {
            Booking booking = deviceService.returnDevice(deviceId);

            return booking;
        } catch (UnavailableDeviceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
