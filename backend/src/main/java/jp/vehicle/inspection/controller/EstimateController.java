package jp.vehicle.inspection.controller;

import jp.vehicle.inspection.domain.entity.Estimate;
import jp.vehicle.inspection.dto.EstimateRequest;
import jp.vehicle.inspection.service.EstimateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estimates")
@RequiredArgsConstructor
public class EstimateController {

    private final EstimateService estimateService;

    @GetMapping
    public List<Estimate> list() {
        return estimateService.findAll();
    }

    @GetMapping("/{id}")
    public Estimate get(@PathVariable Long id) {
        return estimateService.findById(id);
    }

    @PostMapping
    public Estimate create(@Valid @RequestBody EstimateRequest request) {
        return estimateService.create(request);
    }
}
