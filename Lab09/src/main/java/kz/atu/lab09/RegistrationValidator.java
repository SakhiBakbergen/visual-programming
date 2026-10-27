package kz.atu.lab09;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;

public final class RegistrationValidator {
    public static ValidationResult validate(
            String name, String email, String ageText,
            String format, boolean consent, String birthDateText) {

        var errors = new LinkedHashMap<String, String>();

        if (name == null || name.trim().length() < 2 || name.trim().length() > 80) {
            errors.put("name", "ФИО: от 2 до 80 символов");
        }

        if (email == null || !email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,5}$")) {
            errors.put("email", "Введите корректный email");
        }

        try {
            int age = Integer.parseInt(ageText.trim());
            if (age < 16 || age > 100) errors.put("age", "Возраст: 16-100");
        } catch (NumberFormatException e) {
            errors.put("age", "Введите число");
        }

        // Вариант 1: Проверка даты рождения (> 16 лет)
        if (birthDateText == null || birthDateText.isBlank()) {
            errors.put("birthDate", "Дата обязательна");
        } else {
            try {
                LocalDate date = LocalDate.parse(birthDateText.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (date.isAfter(LocalDate.now().minusYears(16))) {
                    errors.put("birthDate", "Студент должен быть старше 16 лет");
                }
            } catch (DateTimeParseException e) {
                errors.put("birthDate", "Формат: ГГГГ-ММ-ДД");
            }
        }

        if (format == null || format.isBlank()) errors.put("format", "Выберите формат");
        if (!consent) errors.put("consent", "Подтвердите согласие");

        return new ValidationResult(errors);
    }
}