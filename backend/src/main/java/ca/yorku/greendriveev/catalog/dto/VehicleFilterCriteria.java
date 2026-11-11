package ca.yorku.greendriveev.catalog.dto;

import ca.yorku.greendriveev.model.AccidentStatus;

public record VehicleFilterCriteria(
    String make, 
    String bodyStyle, 
    Integer year, 
    AccidentStatus accidentStatus){

    }