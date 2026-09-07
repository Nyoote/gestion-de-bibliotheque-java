package com.formation.loan.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoanRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void requestValide_aucuneViolation() {
        LoanRequest request = new LoanRequest("Bob", 3L);

        Set<ConstraintViolation<LoanRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void memberNameVide_violation() {
        LoanRequest request = new LoanRequest("", 3L);

        Set<ConstraintViolation<LoanRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("memberName");
    }

    @Test
    void memberNameNull_violation() {
        LoanRequest request = new LoanRequest(null, 3L);

        Set<ConstraintViolation<LoanRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("memberName");
    }

    @Test
    void bookIdNull_violation() {
        LoanRequest request = new LoanRequest("Bob", null);

        Set<ConstraintViolation<LoanRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("bookId");
    }
}
