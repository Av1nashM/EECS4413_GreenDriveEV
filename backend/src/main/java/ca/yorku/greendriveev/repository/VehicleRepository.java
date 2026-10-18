package ca.yorku.greendriveev.repository;

import ca.yorku.greendriveev.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
