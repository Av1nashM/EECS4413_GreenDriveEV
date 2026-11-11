package ca.yorku.greendriveev.catalog.service;

import ca.yorku.greendriveev.catalog.dto.VehicleFilterCriteria;
import ca.yorku.greendriveev.catalog.dto.VehicleSummaryDTO;

import java.util.List;


public interface CatalogQueryService {

    List<VehicleSummaryDTO> listVehicles(
            VehicleFilterCriteria criteria,
            String sortBy,
            String order);
}