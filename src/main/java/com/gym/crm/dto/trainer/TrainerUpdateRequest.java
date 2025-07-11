package com.gym.crm.dto.trainer;

import com.gym.crm.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainerUpdateRequest {
    private static final String USERNAME_PATTERN = "^[a-zA-Z]+\\.[a-zA-Z]+$";

    @NotNull(message = "ID is required")
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Pattern(
            regexp = USERNAME_PATTERN,
            message = "Username must be in the format 'firstname.lastname'"
    )
    private String username;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    @NotNull(message = "Specialization is required")
    private TrainingType specialization;
}
