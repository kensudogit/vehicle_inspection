package jp.vehicle.inspection.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EstimateItemRequest {
    private String itemType;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private Long partId;
}
