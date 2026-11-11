package ca.yorku.greendriveev.catalog.dto;

import ca.yorku.greendriveev.model.AccidentStatus;

public record VehicleSummaryDTO(
        Long id,
        String make,
        String model,
        int year,
        double price,
        int mileage,
        String bodyStyle,
        AccidentStatus accidentStatus,
        String imageUrl) {
}