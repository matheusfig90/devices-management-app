package com.matheusfig90.service;

import com.matheusfig90.entity.Booking;
import com.matheusfig90.entity.Device;
import com.matheusfig90.entity.User;
import com.matheusfig90.exceptions.EntityNotFoundException;
import com.matheusfig90.exceptions.UnavailableDeviceException;
import com.matheusfig90.repository.BookingRepository;
import com.matheusfig90.repository.DeviceRepository;
import com.matheusfig90.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class DeviceService {
    public record DeviceInfo(Device device, Booking latestBooking,
                      Boolean isDeviceAvailable) {
    }

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    public DeviceService(BookingRepository bookingRepository, DeviceRepository deviceRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
   }

   public DeviceInfo getInfo(Long deviceId) throws EntityNotFoundException {
        Device device = getDevice(deviceId);
        Booking latestBooking = getLatestBookingOrNull(deviceId);

        return new DeviceInfo(device, latestBooking, isDeviceAvailable(latestBooking));
   }

    public Booking bookDevice(Long deviceId, Long userId) throws EntityNotFoundException, UnavailableDeviceException {
        Device device = getDevice(deviceId);
        User user = getUser(userId);

        Booking latestBooking = getLatestBookingOrNull(deviceId);
        if (!isDeviceAvailable(latestBooking)) {
            throw new UnavailableDeviceException("Device is already booked");
        }

        Booking booking = Booking.builder()
                .device(device)
                .user(user)
                .bookedAt(LocalDateTime.now())
                .build();

        return bookingRepository.save(booking);
    }

    public Booking returnDevice(Long deviceId) throws UnavailableDeviceException {
        Booking latestBooking = getLatestBookingOrNull(deviceId);
        if (isDeviceAvailable(latestBooking)) {
            throw new UnavailableDeviceException("Device is available, no return needed");
        }

        latestBooking.setReturnedAt(LocalDateTime.now());

        return bookingRepository.save(latestBooking);
    }

    private Device getDevice(Long deviceId) throws EntityNotFoundException {
        Optional<Device> device = deviceRepository.findById(deviceId);
        if (device.isEmpty()) {
            throw new EntityNotFoundException("Device not found");
        }

        return device.get();
    }

    private User getUser(Long userId) throws EntityNotFoundException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        return user.get();
    }

    private Booking getLatestBookingOrNull(Long deviceId) {
        Optional<Booking> latestBooking = bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(deviceId);
        if (latestBooking.isEmpty()) {
            return null;
        }

        return latestBooking.get();
    }

    private Boolean isDeviceAvailable(Booking latestBooking) {
        return Objects.isNull(latestBooking) || !Objects.isNull(latestBooking.getReturnedAt());
    }
}
