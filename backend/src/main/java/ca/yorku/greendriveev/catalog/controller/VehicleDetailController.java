package ca.yorku.greendriveev.catalog.controller;

import ca.yorku.greendriveev.catalog.dto.VehicleDetailDTO;
import ca.yorku.greendriveev.catalog.service.VehicleDetailService;
import org.springframework.web.bind.annotation.*;

/** PB-08: public vehicle details endpoint. */
@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class VehicleDetailController {

    private final VehicleDetailService vehicleDetailService;

    public VehicleDetailController(VehicleDetailService vehicleDetailService) {
        this.vehicleDetailService = vehicleDetailService;
    }

    @GetMapping("/{id}")
    public VehicleDetailDTO getVehicle(@PathVariable Long id) {
        return vehicleDetailService.getVehicleDetails(id);
    }
}
