package com.matheusfig90.service;

import com.matheusfig90.entity.Booking;
import com.matheusfig90.entity.Device;
import com.matheusfig90.entity.User;
import com.matheusfig90.exceptions.EntityNotFoundException;
import com.matheusfig90.exceptions.UnavailableDeviceException;
import com.matheusfig90.repository.BookingRepository;
import com.matheusfig90.repository.DeviceRepository;
import com.matheusfig90.repository.UserRepository;
import com.matheusfig90.service.DeviceService.DeviceInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTest {
    private static Long DEVICE_ID = 1L;
    private static Long USER_ID = 1L;

    private Device device = Device.builder()
            .id(DEVICE_ID)
            .name("iPhone 15 Pro")
            .build();

    private User user = User.builder()
            .id(USER_ID)
            .name("User #1")
            .build();

    Booking bookingWithoutReturn = Booking.builder()
            .id(1L)
            .device(device)
            .user(user)
            .bookedAt(LocalDateTime.now().minus(15, ChronoUnit.MINUTES))
            .build();

    Booking bookingWithReturn = Booking.builder()
            .id(1L)
            .device(device)
            .user(user)
            .bookedAt(LocalDateTime.now().minus(15, ChronoUnit.MINUTES))
            .returnedAt(LocalDateTime.now())
            .build();

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    private DeviceService deviceService;

    @BeforeEach
    public void setUp() {
        deviceService = new DeviceService(
                bookingRepository,
                deviceRepository,
                userRepository
        );
    }

    @Test
    public void shouldBookDevice() throws EntityNotFoundException, UnavailableDeviceException {
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any())).thenReturn(bookingWithoutReturn);
        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(any())).thenReturn(Optional.empty());

        Booking bookingResponse = deviceService.bookDevice(DEVICE_ID, USER_ID);

        assertEquals(bookingResponse, bookingWithoutReturn);

        verify(bookingRepository, times(1)).save(any());
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    public void shouldBookDeviceWhichWasReturned() throws EntityNotFoundException, UnavailableDeviceException {
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any())).thenReturn(bookingWithoutReturn);
        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(any())).thenReturn(Optional.of(bookingWithReturn));

        Booking bookingResponse = deviceService.bookDevice(DEVICE_ID, USER_ID);

        assertEquals(bookingResponse, bookingWithoutReturn);

        verify(bookingRepository, times(1)).save(any());
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    public void shouldFailOnBookingDeviceAlreadyBooked() {
        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(DEVICE_ID)).thenReturn(Optional.of(bookingWithoutReturn));
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(UnavailableDeviceException.class, () -> deviceService.bookDevice(DEVICE_ID, USER_ID));
    }

    @Test
    public void  shouldFailOnBookingDeviceWithDeviceIdOrUserIdInvalid() {
        Long invalidId = 999L;

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(deviceRepository.findById(invalidId)).thenReturn(Optional.empty());
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> deviceService.bookDevice(invalidId, USER_ID));
        assertThrows(EntityNotFoundException.class, () -> deviceService.bookDevice(DEVICE_ID, invalidId));
    }

    @Test
    public void shouldReturnDevice() throws UnavailableDeviceException {
        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(DEVICE_ID)).thenReturn(Optional.of(bookingWithoutReturn));
        when(bookingRepository.save(any())).thenReturn(bookingWithReturn);

        Booking bookingResponse = deviceService.returnDevice(DEVICE_ID);

        assertEquals(bookingResponse, bookingWithReturn);

        verify(bookingRepository, times(1)).findTopByDeviceIdOrderByBookedAtDesc(DEVICE_ID);
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    public void shouldFailOnReturnDeviceAlreadyReturned() {
        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(DEVICE_ID)).thenReturn(Optional.of(bookingWithReturn));

        assertThrows(UnavailableDeviceException.class, () -> deviceService.returnDevice(DEVICE_ID));

        verify(bookingRepository, times(1)).findTopByDeviceIdOrderByBookedAtDesc(DEVICE_ID);
    }

    @Test
    public void shouldFailOnReturningDeviceWithDeviceIdInvalid() {
        Long invalidId = 999L;

        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(invalidId)).thenReturn(Optional.empty());

        assertThrows(UnavailableDeviceException.class, () -> deviceService.returnDevice(invalidId));
    }

    @Test
    public void shouldFetchInfoFromAvailableDevice() throws EntityNotFoundException {
        DeviceInfo expectedDeviceInfo = new DeviceInfo(device, bookingWithReturn, true);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(DEVICE_ID)).thenReturn(Optional.of(bookingWithReturn));

        DeviceInfo deviceInfoResponse = deviceService.getInfo(DEVICE_ID);

        assertEquals(deviceInfoResponse, expectedDeviceInfo);
    }

    @Test
    public void shouldFetchInfoFromUnavailableDevice() throws EntityNotFoundException {
        DeviceInfo expectedDeviceInfo = new DeviceInfo(device, bookingWithoutReturn, false);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(DEVICE_ID)).thenReturn(Optional.of(bookingWithoutReturn));

        DeviceInfo deviceInfoResponse = deviceService.getInfo(DEVICE_ID);

        assertEquals(deviceInfoResponse, expectedDeviceInfo);
    }

    @Test
    public void shouldFetchInfoFromDeviceNeverBooked() throws EntityNotFoundException {
        DeviceInfo expectedDeviceInfo = new DeviceInfo(device, null, true);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(bookingRepository.findTopByDeviceIdOrderByBookedAtDesc(DEVICE_ID)).thenReturn(Optional.empty());

        DeviceInfo deviceInfoResponse = deviceService.getInfo(DEVICE_ID);

        assertEquals(deviceInfoResponse, expectedDeviceInfo);
    }

    @Test
    public void shouldFailOnFetchInfoFromInvalidDevice() {
        Long invalidId = 999L;

        when(deviceRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> deviceService.getInfo(invalidId));
    }
}
