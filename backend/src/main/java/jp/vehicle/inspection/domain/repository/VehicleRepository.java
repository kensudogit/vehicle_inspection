package jp.vehicle.inspection.domain.repository;

import jp.vehicle.inspection.domain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    Optional<Vehicle> findByChassisNumber(String chassisNumber);
    List<Vehicle> findByCustomerId(Long customerId);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByChassisNumber(String chassisNumber);

    @Query("SELECT v FROM Vehicle v WHERE v.active = true AND v.inspectionExpiry BETWEEN :from AND :to")
    List<Vehicle> findExpiringBetween(LocalDate from, LocalDate to);
}
