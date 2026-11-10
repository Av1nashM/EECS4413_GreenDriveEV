package ca.yorku.greendriveev.catalog.service;

import ca.yorku.greendriveev.catalog.dto.VehicleDetailDTO;

public interface VehicleDetailService {

    /** @throws ca.yorku.greendriveev.catalog.exception.VehicleNotFoundException if absent */
    VehicleDetailDTO getVehicleDetails(Long id);
}
