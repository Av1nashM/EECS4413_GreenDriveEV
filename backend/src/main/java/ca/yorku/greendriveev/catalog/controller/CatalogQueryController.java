package ca.yorku.greendriveev.catalog.controller;

import ca.yorku.greendriveev.catalog.dto.VehicleFilterCriteria;
import ca.yorku.greendriveev.catalog.dto.VehicleSummaryDTO;
import ca.yorku.greendriveev.catalog.service.CatalogQueryService;
import ca.yorku.greendriveev.model.AccidentStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class CatalogQueryController {

    private final CatalogQueryService catalogQueryService;

    public CatalogQueryController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping
    public List<VehicleSummaryDTO> listVehicles(
            @RequestParam(name = "make", required = false)
            String make,

            @RequestParam(name = "bodyStyle", required = false)
            String bodyStyle,

            @RequestParam(name = "year", required = false)
            Integer year,

            @RequestParam(name = "accidentStatus", required = false)
            AccidentStatus accidentStatus,

            @RequestParam(name = "sortBy", defaultValue = "price")
            String sortBy,

            @RequestParam(name = "order", defaultValue = "asc")
            String order) {

        VehicleFilterCriteria criteria = new VehicleFilterCriteria(
                make,
                bodyStyle,
                year,
                accidentStatus
        );

        return catalogQueryService.listVehicles(criteria, sortBy, order);
    }
}