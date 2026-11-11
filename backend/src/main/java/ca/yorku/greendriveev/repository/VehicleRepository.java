package ca.yorku.greendriveev.repository;

import ca.yorku.greendriveev.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleRepository
        extends JpaRepository<Vehicle, Long>,
                JpaSpecificationExecutor<Vehicle> {
}