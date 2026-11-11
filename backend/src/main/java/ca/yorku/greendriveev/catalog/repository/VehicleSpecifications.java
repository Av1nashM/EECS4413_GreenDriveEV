package ca.yorku.greendriveev.catalog.repository;

import ca.yorku.greendriveev.catalog.dto.VehicleFilterCriteria;
import ca.yorku.greendriveev.model.AccidentStatus;
import ca.yorku.greendriveev.model.Vehicle;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> matching(
            VehicleFilterCriteria criteria) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String make = normalize(criteria.make());
            if (make != null) {
                predicates.add(
                        cb.equal(cb.lower(root.<String>get("make")), make)
                );
            }

            String bodyStyle = normalize(criteria.bodyStyle());
            if (bodyStyle != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.<String>get("bodyStyle")),
                                bodyStyle
                        )
                );
            }

            if (criteria.year() != null) {
                predicates.add(
                        cb.equal(root.<Integer>get("year"), criteria.year())
                );
            }

            AccidentStatus status = criteria.accidentStatus();

            if (status == AccidentStatus.UNKNOWN) {
                predicates.add(
                        cb.or(
                                cb.isNull(root.get("accidentStatus")),
                                cb.equal(
                                        root.get("accidentStatus"),
                                        AccidentStatus.UNKNOWN
                                )
                        )
                );
            } else if (status != null) {
                predicates.add(
                        cb.equal(root.get("accidentStatus"), status)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }
}