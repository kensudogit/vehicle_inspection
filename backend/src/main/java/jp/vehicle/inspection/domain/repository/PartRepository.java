package jp.vehicle.inspection.domain.repository;

import jp.vehicle.inspection.domain.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {
    Optional<Part> findByPartCode(String partCode);
}
