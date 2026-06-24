package jp.vehicle.inspection.domain.repository;

import jp.vehicle.inspection.domain.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
