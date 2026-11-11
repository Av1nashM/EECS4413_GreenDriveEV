package ca.yorku.greendriveev.catalog.service;

import ca.yorku.greendriveev.catalog.dto.VehicleFilterCriteria;
import ca.yorku.greendriveev.catalog.dto.VehicleSummaryDTO;
import ca.yorku.greendriveev.catalog.exception.InvalidCatalogQueryException;
import ca.yorku.greendriveev.catalog.repository.VehicleSpecifications;
import ca.yorku.greendriveev.model.Vehicle;
import ca.yorku.greendriveev.repository.VehicleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CatalogQueryServiceImpl implements CatalogQueryService {

    private final VehicleRepository vehicleRepository;

    public CatalogQueryServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleSummaryDTO> listVehicles(
            VehicleFilterCriteria criteria,
            String sortBy,
            String order) {

        validateCriteria(criteria);
        Sort sort = buildSort(sortBy, order);

        return vehicleRepository
                .findAll(VehicleSpecifications.matching(criteria), sort)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private void validateCriteria(VehicleFilterCriteria criteria) {
        if (criteria == null) {
            throw new InvalidCatalogQueryException(
                    "Filter criteria must be provided."
            );
        }

        Integer year = criteria.year();

        if (year != null && (year < 1 || year > 9999)) {
            throw new InvalidCatalogQueryException(
                    "year must be between 1 and 9999."
            );
        }
    }

    private Sort buildSort(String sortBy, String order) {
        String field = normalizeOrDefault(sortBy, "price");
        String direction = normalizeOrDefault(order, "asc");

        if (!field.equals("price") && !field.equals("mileage")) {
            throw new InvalidCatalogQueryException(
                    "sortBy must be price or mileage."
            );
        }

        Sort.Direction sortDirection;

        switch (direction) {
            case "asc" -> sortDirection = Sort.Direction.ASC;
            case "desc" -> sortDirection = Sort.Direction.DESC;
            default -> throw new InvalidCatalogQueryException(
                    "order must be asc or desc."
            );
        }

        return Sort.by(sortDirection, field)
                .and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private VehicleSummaryDTO toSummary(Vehicle vehicle) {
        return new VehicleSummaryDTO(
                vehicle.getId(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getPrice(),
                vehicle.getMileage(),
                vehicle.getBodyStyle(),
                vehicle.getAccidentStatus(),
                vehicle.getImageUrl()
        );
    }
}