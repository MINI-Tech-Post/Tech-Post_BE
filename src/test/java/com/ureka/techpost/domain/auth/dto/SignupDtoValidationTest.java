package com.ureka.techpost.domain.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validator;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignupDtoValidationTest {
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void signup_username_blank_then_violation() {
        SignupDto dto = new SignupDto();
        dto.setUsername("");
        dto.setPassword("password1");
        dto.setName("홍길동");

        Set<ConstraintViolation<SignupDto>> violations =
                validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void signup_valid_then_no_violation() {
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword("password1");
        dto.setName("홍길동");

        Set<ConstraintViolation<SignupDto>> violations =
                validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
    @Test
    void signup_shortPassword() {
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword("pass");
        dto.setName("홍길동");

        Set<ConstraintViolation<SignupDto>> violations =
                validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void signup_shortUsername() {
        SignupDto dto = new SignupDto();
        dto.setUsername("tes");
        dto.setPassword("pass");
        dto.setName("홍길동");

        Set<ConstraintViolation<SignupDto>> violations =
                validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
}
