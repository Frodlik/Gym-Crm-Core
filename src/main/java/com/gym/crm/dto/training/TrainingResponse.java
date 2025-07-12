package com.gym.crm.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainingResponse {
    private Long id;
    private String traineeUsername;
    private String trainerUsername;
    private String trainingName;
    private String trainingTypeName;
    private LocalDate trainingDate;
    private Integer trainingDuration;
}
