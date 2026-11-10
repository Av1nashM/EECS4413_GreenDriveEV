package ca.yorku.greendriveev.catalog;

import ca.yorku.greendriveev.model.Vehicle;
import ca.yorku.greendriveev.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleDetailApiTest {

    @Autowired MockMvc mvc;
    @Autowired VehicleRepository vehicles;

    @Test
    void existingVehicle_returns200_withHistory_withoutLogin() throws Exception {
        Vehicle saved = vehicles.save(new Vehicle("Tesla", "Model 3", 2021, 38999.0, 42000,
                "Sedan", "No accidents. One previous owner. Battery health 94%.", "http://img/t3.png"));

        mvc.perform(get("/api/v1/vehicles/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.make").value("Tesla"))
                .andExpect(jsonPath("$.model").value("Model 3"))
                .andExpect(jsonPath("$.year").value(2021))
                .andExpect(jsonPath("$.price").value(38999.0))
                .andExpect(jsonPath("$.mileage").value(42000))
                .andExpect(jsonPath("$.bodyStyle").value("Sedan"))
                .andExpect(jsonPath("$.historyReport").value("No accidents. One previous owner. Battery health 94%."))
                .andExpect(jsonPath("$.imageUrl").value("http://img/t3.png"));
    }

    @Test
    void missingVehicle_returns404_inStandardErrorFormat() throws Exception {
        mvc.perform(get("/api/v1/vehicles/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Vehicle not found with ID: 999999"))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles/999999"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void nonNumericId_returns400() throws Exception {
        mvc.perform(get("/api/v1/vehicles/abc"))
                .andExpect(status().isBadRequest());
    }
}
