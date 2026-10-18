package ca.yorku.greendriveev.controller;

import ca.yorku.greendriveev.model.Vehicle;
import ca.yorku.greendriveev.repository.VehicleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleRepository repo;

    public VehicleController(VehicleRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Vehicle getVehicle(@PathVariable Long id) {
        return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with ID: "+id));
    }
}
