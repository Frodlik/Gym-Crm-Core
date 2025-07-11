package com.gym.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";

    @NotBlank(message = "Username is required")
    private String username;

    @Size(min = 10, message = "Password must be at least 10 characters long")
    @Pattern(
            regexp = PASSWORD_PATTERN,
            message = "Password must include uppercase, lowercase letters and a number"
    )
    private String oldPassword;

    @Size(min = 10, message = "Password must be at least 10 characters long")
    @Pattern(
            regexp = PASSWORD_PATTERN,
            message = "Password must include uppercase, lowercase letters and a number"
    )
    private String newPassword;
}
