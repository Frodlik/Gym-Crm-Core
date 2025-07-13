package com.gym.crm.dto.trainee;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeTrainersUpdateRequest {
    @NotNull(message = "Trainee username cannot be null")
    private String traineeUsername;

    @NotNull(message = "Trainer usernames list cannot be null")
    @NotEmpty(message = "Trainer usernames list cannot be empty")
    private List<String> trainerUsernames;
}