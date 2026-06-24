package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.Document;
import jp.vehicle.inspection.domain.entity.Vehicle;
import jp.vehicle.inspection.domain.entity.VehicleInspection;
import jp.vehicle.inspection.domain.repository.DocumentRepository;
import jp.vehicle.inspection.domain.repository.VehicleInspectionRepository;
import jp.vehicle.inspection.domain.repository.VehicleRepository;
import jp.vehicle.inspection.exception.BusinessException;
import jp.vehicle.inspection.integration.ElectronicInspectionClient;
import jp.vehicle.inspection.integration.storage.StorageService;
import jp.vehicle.inspection.security.CurrentUserService;
import jp.vehicle.inspection.util.VehicleValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public List<Document> findByEntity(String entityType, Long entityId) {
        return documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Transactional
    public Document upload(String entityType, Long entityId, String documentType, MultipartFile file) throws Exception {
        String key = entityType.toLowerCase() + "/" + entityId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        storageService.upload(key, file);
        Document doc = Document.builder()
                .entityType(entityType)
                .entityId(entityId)
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .storageKey(key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .checksumSha256(sha256(file.getBytes()))
                .uploadedAt(OffsetDateTime.now())
                .uploadedBy(currentUserService.getCurrentUserId())
                .build();
        Document saved = documentRepository.save(doc);
        auditService.log("UPLOAD", "DOCUMENT", saved.getId(), null, saved);
        return saved;
    }

    private String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }
}
