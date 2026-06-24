package jp.vehicle.inspection.controller;

import jp.vehicle.inspection.domain.entity.Document;
import jp.vehicle.inspection.domain.entity.VehicleInspection;
import jp.vehicle.inspection.service.DocumentService;
import jp.vehicle.inspection.service.ElectronicInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final ElectronicInspectionService electronicInspectionService;

    @GetMapping("/documents")
    public List<Document> list(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        return documentService.findByEntity(entityType, entityId);
    }

    @PostMapping("/documents/upload")
    public Document upload(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam String documentType,
            @RequestParam MultipartFile file) throws Exception {
        return documentService.upload(entityType, entityId, documentType, file);
    }

    @GetMapping("/electronic-inspections/vehicle/{vehicleId}")
    public List<VehicleInspection> listInspections(@PathVariable Long vehicleId) {
        return electronicInspectionService.findByVehicle(vehicleId);
    }

    @PostMapping("/electronic-inspections/import")
    public VehicleInspection importCert(@RequestBody Map<String, Object> body) {
        Long vehicleId = Long.valueOf(body.get("vehicleId").toString());
        String certId = body.get("certId").toString();
        return electronicInspectionService.importByCertId(vehicleId, certId);
    }
}
