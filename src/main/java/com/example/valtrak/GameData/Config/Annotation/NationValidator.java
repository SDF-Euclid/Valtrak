package com.example.valtrak.GameData.Config.Annotation;

import com.example.valtrak.GameData.Repository.EnumData.NationRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NationValidator implements ConstraintValidator<ValidNation, String> {

    private final NationRepository nationRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return nationRepository.existsByNationName(value);
    }
}