package jp.vehicle.inspection.domain.repository;

import jp.vehicle.inspection.domain.entity.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    Optional<Estimate> findByEstimateNumber(String estimateNumber);
    List<Estimate> findByCustomerId(Long customerId);
}
