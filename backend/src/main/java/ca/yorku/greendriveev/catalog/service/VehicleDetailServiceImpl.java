package ca.yorku.greendriveev.catalog.service;

import ca.yorku.greendriveev.catalog.dto.VehicleDetailDTO;
import ca.yorku.greendriveev.catalog.exception.VehicleNotFoundException;
import ca.yorku.greendriveev.model.Vehicle;
import ca.yorku.greendriveev.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleDetailServiceImpl implements VehicleDetailService {

    private final VehicleRepository vehicleRepository;

    public VehicleDetailServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleDetailDTO getVehicleDetails(Long id) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        return new VehicleDetailDTO(
                v.getId(), v.getMake(), v.getModel(), v.getYear(),
                v.getPrice(), v.getMileage(), v.getBodyStyle(),
                v.getHistory(), v.getImageUrl());
    }
}
