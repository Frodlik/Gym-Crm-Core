package com.gym.crm.service;

public interface AuthenticationService {
    String validateCredentials(String username, String password);

    void validateTraineeCredentials(String username, String password);

    void validateTrainerCredentials(String username, String password);
}
