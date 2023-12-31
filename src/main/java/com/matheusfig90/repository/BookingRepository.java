package com.matheusfig90.repository;

import com.matheusfig90.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findTopByDeviceIdOrderByBookedAtDesc(Long deviceId);
}
