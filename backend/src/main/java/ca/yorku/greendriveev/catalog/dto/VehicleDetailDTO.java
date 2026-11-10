package ca.yorku.greendriveev.catalog.dto;

/** Full vehicle details (PB-08). Field names match the existing Vehicle API. */
public record VehicleDetailDTO(
        Long id,
        String make,
        String model,
        int year,
        double price,
        int mileage,
        String bodyStyle,
        String historyReport,
        String imageUrl) {
}
