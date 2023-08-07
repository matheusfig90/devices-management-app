package com.matheusfig90.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheusfig90.controllers.requests.BookDeviceRequestBody;
import com.matheusfig90.entity.Booking;
import com.matheusfig90.entity.Device;
import com.matheusfig90.entity.User;
import com.matheusfig90.repository.BookingRepository;
import com.matheusfig90.repository.DeviceRepository;
import com.matheusfig90.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeviceControllerIntegrationTest {

    private static Long USER_ID = 1L;
    private static String USER_NAME = "User #1";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    public void setUp() {
        User user1 = User.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .build();

        userRepository.save(user1);

        // Device #1 and #4 are available
        // Device #2 and #3 are booked, but #3 will be return
        Device device1 = Device.builder()
                .id(1L)
                .name("Device #1")
                .build();

        Device device2 = Device.builder()
                .id(2L)
                .name("Device #2")
                .build();

        Device device3 = Device.builder()
                .id(3L)
                .name("Device #3")
                .build();

        Device device4 = Device.builder()
                .id(4L)
                .name("Device #4")
                .build();

        deviceRepository.saveAll(Arrays.asList(device1, device2, device3, device4));

        // Device #2 is booked
        Booking booking1 = Booking.builder()
                .device(device2)
                .user(user1)
                .bookedAt(LocalDateTime.now().minus(15, ChronoUnit.MINUTES))
                .build();

        // Device #3 is booked and it will be return during tests
        Booking booking2 = Booking.builder()
                .device(device3)
                .user(user1)
                .bookedAt(LocalDateTime.now().minus(15, ChronoUnit.MINUTES))
                .build();

        // Device #4 is available, but there was a booking in the past
        Booking booking3 = Booking.builder()
                .device(device4)
                .user(user1)
                .bookedAt(LocalDateTime.now().minus(15, ChronoUnit.MINUTES))
                .returnedAt(LocalDateTime.now())
                .build();

        bookingRepository.saveAll(Arrays.asList(booking1, booking2, booking3));
    }

    @Nested
    public class GetInfoFromDevices {
        @Test
        public void getInfoFromAvailableDevice() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .get("/devices/4")
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.device.name", is("Device #4")))
                    .andExpect(jsonPath("$.latestBooking.user.name", is(USER_NAME)))
                    .andExpect(jsonPath("$.isAvailable", is(true)));
        }

        @Test
        public void getInfoFromBookedDevice() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .get("/devices/2")
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.device.name", is("Device #2")))
                    .andExpect(jsonPath("$.latestBooking.user.name", is(USER_NAME)))
                    .andExpect(jsonPath("$.isAvailable", is(false)));
        }

        @Test
        public void failsOnGetInfoUsingWrongId() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .get("/devices/999")
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    public class BookDevices {
        private BookDeviceRequestBody requestBody = new BookDeviceRequestBody(USER_ID);

        @Test
        public void bookSuccessfully() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .put("/devices/1/book")
                    .content(new ObjectMapper().writeValueAsString(requestBody))
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.device.name", is("Device #1")))
                    .andExpect(jsonPath("$.user.name", is(USER_NAME)));
        }

        @Test
        public void failOnBookAnUnavailableDevice() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .put("/devices/2/book")
                    .content(new ObjectMapper().writeValueAsString(requestBody))
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void failsOnBookWithWrongDeviceId() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .put("/devices/999/book")
                    .content(new ObjectMapper().writeValueAsString(requestBody))
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        @Test
        public void failsOnBookWithWrongUserId() throws Exception {
            requestBody = new BookDeviceRequestBody(999L);

            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .put("/devices/1/book")
                    .content(new ObjectMapper().writeValueAsString(requestBody))
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    public class ReturnDevices {
        @Test
        public void returnSuccessfully() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .put("/devices/3/return")
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.device.name", is("Device #3")))
                    .andExpect(jsonPath("$.returnedAt", notNullValue()));
        }

        @Test
        public void failOnReturnAnAvailableDevice() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .put("/devices/4/return")
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void failsOnReturnWithWrongDeviceId() throws Exception {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                    .put("/devices/999/return")
                    .contentType(MediaType.APPLICATION_JSON);

            mvc.perform(request)
                    .andExpect(status().isBadRequest());
        }
    }
}
