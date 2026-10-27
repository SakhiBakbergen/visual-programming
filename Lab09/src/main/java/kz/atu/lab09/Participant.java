package kz.atu.lab09;

import java.time.LocalDate;

public class Participant {
    private String name;
    private String email;
    private int age;
    private String format;
    private LocalDate birthDate;

    public Participant(String name, String email, int age, String format, LocalDate birthDate) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.format = format;
        this.birthDate = birthDate;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public String getFormat() { return format; }
    public LocalDate getBirthDate() { return birthDate; }
}