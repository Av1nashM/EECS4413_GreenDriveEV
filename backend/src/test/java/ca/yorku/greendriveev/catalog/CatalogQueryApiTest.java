package ca.yorku.greendriveev.catalog;

import ca.yorku.greendriveev.model.AccidentStatus;
import ca.yorku.greendriveev.model.Vehicle;
import ca.yorku.greendriveev.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:catalog-query-tests;"
                + "MODE=PostgreSQL;NON_KEYWORDS=YEAR;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogQueryApiTest {

    private static final String URL = "/api/v1/vehicles";

    @Autowired
    MockMvc mvc;

    @Autowired
    VehicleRepository vehicles;

    @Autowired
    EntityManager entityManager;

    private Vehicle model3;

    @BeforeEach
    void setUp() {
        model3 = saveVehicle(
                "Tesla", "Model 3", 2021, 40000, 30000,
                "Sedan", AccidentStatus.NO_REPORTED_ACCIDENTS
        );

        saveVehicle(
                "Hyundai", "Ioniq 5", 2023, 50000, 10000,
                "SUV", AccidentStatus.ACCIDENT_REPORTED
        );

        saveVehicle(
                "Tesla", "Model Y", 2023, 60000, 20000,
                "SUV", AccidentStatus.NO_REPORTED_ACCIDENTS
        );

        saveVehicle(
                "Kia", "Niro", 2020, 30000, 40000,
                "SUV", AccidentStatus.UNKNOWN
        );

        Vehicle leaf = saveVehicle(
                "Nissan", "Leaf", 2019, 20000, 50000,
                "Hatchback", AccidentStatus.UNKNOWN
        );

        entityManager.flush();

        int updated = entityManager.createNativeQuery(
                        "UPDATE vehicles SET accident_status = NULL WHERE id = :id"
                )
                .setParameter("id", leaf.getId())
                .executeUpdate();

        assertEquals(1, updated);

        entityManager.clear();
    }

    @Test
    void browse_withoutLogin_returnsSummaries_sortedByPriceAscending()
            throws Exception {

        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[*].model", contains(
                        "Leaf", "Niro", "Model 3", "Ioniq 5", "Model Y"
                )))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].make").value("Nissan"))
                .andExpect(jsonPath("$[0].year").value(2019))
                .andExpect(jsonPath("$[0].price").value(20000.0))
                .andExpect(jsonPath("$[0].mileage").value(50000))
                .andExpect(jsonPath("$[0].bodyStyle").value("Hatchback"))
                .andExpect(jsonPath("$[0].accidentStatus").value("UNKNOWN"))
                .andExpect(jsonPath("$[0].imageUrl")
                        .value("https://example.com/vehicle.png"));
    }

    @ParameterizedTest
    @CsvSource({
            "price, asc, Leaf|Niro|Model 3|Ioniq 5|Model Y",
            "price, desc, Model Y|Ioniq 5|Model 3|Niro|Leaf",
            "mileage, asc, Ioniq 5|Model Y|Model 3|Niro|Leaf",
            "mileage, desc, Leaf|Niro|Model 3|Model Y|Ioniq 5"
    })
    void sorting_returnsExpectedOrder(
            String sortBy,
            String order,
            String expectedModels) throws Exception {

        mvc.perform(get(URL)
                        .param("sortBy", sortBy)
                        .param("order", order))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[*].model",
                        contains(expectedModels.split("\\|"))
                ));
    }

    @ParameterizedTest
    @CsvSource({
            "make, ' tesLA ', Model 3|Model Y",
            "bodyStyle, ' sUv ', Niro|Ioniq 5|Model Y",
            "year, 2023, Ioniq 5|Model Y",
            "accidentStatus, NO_REPORTED_ACCIDENTS, Model 3|Model Y",
            "accidentStatus, ACCIDENT_REPORTED, Ioniq 5",
            "accidentStatus, UNKNOWN, Leaf|Niro"
    })
    void individualFilters_returnOnlyMatchingVehicles(
            String parameter,
            String value,
            String expectedModels) throws Exception {

        mvc.perform(get(URL).param(parameter, value))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[*].model",
                        contains(expectedModels.split("\\|"))
                ));
    }

    @Test
    void combinedFilters_applyTogether() throws Exception {
        mvc.perform(get(URL)
                        .param("make", "Tesla")
                        .param("bodyStyle", "SUV")
                        .param("year", "2023")
                        .param("accidentStatus", "NO_REPORTED_ACCIDENTS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].model", contains("Model Y")));

        mvc.perform(get(URL)
                        .param("make", "Tesla")
                        .param("bodyStyle", "SUV")
                        .param("year", "2021"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unknownStatus_includesNullRows_andReturnsUnknownInJson()
            throws Exception {

        mvc.perform(get(URL).param("accidentStatus", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].model", contains("Leaf", "Niro")))
                .andExpect(jsonPath(
                        "$[*].accidentStatus",
                        contains("UNKNOWN", "UNKNOWN")
                ));
    }

    @Test
    void noMatchingVehicles_returns200WithEmptyArray() throws Exception {
        mvc.perform(get(URL).param("make", "NonexistentBrand"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void emptyCatalogue_returns200WithEmptyArray() throws Exception {
        vehicles.deleteAll();
        vehicles.flush();

        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void blankFiltersAndSorting_useDefaults() throws Exception {
        mvc.perform(get(URL)
                        .param("make", " ")
                        .param("bodyStyle", "")
                        .param("sortBy", " ")
                        .param("order", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].model", contains(
                        "Leaf", "Niro", "Model 3", "Ioniq 5", "Model Y"
                )));
    }

    @ParameterizedTest
    @CsvSource({
            "price, Model 3|Model S|Model Y",
            "mileage, Model Y|Model 3|Model S"
    })
    void equalSortValues_areOrderedById(
            String sortBy,
            String expectedModels) throws Exception {

        saveVehicle(
                "Tesla", "Model S", 2022, 40000, 30000,
                "Sedan", AccidentStatus.UNKNOWN
        );

        mvc.perform(get(URL)
                        .param("make", "Tesla")
                        .param("sortBy", sortBy)
                        .param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[*].model",
                        contains(expectedModels.split("\\|"))
                ));
    }

    @ParameterizedTest
    @CsvSource({
            "sortBy, model, sortBy must be price or mileage.",
            "order, sideways, order must be asc or desc.",
            "year, 0, year must be between 1 and 9999.",
            "year, 10000, year must be between 1 and 9999.",
            "year, abc, Malformed request.",
            "accidentStatus, INVALID, Malformed request."
    })
    void invalidParameters_return400WithStandardError(
            String parameter,
            String value,
            String expectedMessage) throws Exception {

        mvc.perform(get(URL).param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(URL))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void existingDetailAndLegacyEndpoints_keepTheirResponseFields()
            throws Exception {

        mvc.perform(get(URL + "/" + model3.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(model3.getId()))
                .andExpect(jsonPath("$.model").value("Model 3"))
                .andExpect(jsonPath("$.historyReport")
                        .value("Test history for Model 3"));

        mvc.perform(get("/api/vehicles/" + model3.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(model3.getId()))
                .andExpect(jsonPath("$.history")
                        .value("Test history for Model 3"))
                .andExpect(jsonPath("$.accidentStatus").doesNotExist());
    }

    private Vehicle saveVehicle(
            String make,
            String model,
            int year,
            double price,
            int mileage,
            String bodyStyle,
            AccidentStatus accidentStatus) {

        Vehicle vehicle = new Vehicle(
                make,
                model,
                year,
                price,
                mileage,
                bodyStyle,
                "Test history for " + model,
                "https://example.com/vehicle.png"
        );

        vehicle.setAccidentStatus(accidentStatus);
        return vehicles.saveAndFlush(vehicle);
    }
}