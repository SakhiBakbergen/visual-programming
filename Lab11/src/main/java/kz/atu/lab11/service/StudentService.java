package kz.atu.lab11.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kz.atu.lab11.model.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class StudentService {
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private static final String FILE_PATH = "students_data.json";

    public StudentService() {
        loadFromFile();
    }

    public ObservableList<Student> getStudents() {
        return students;
    }

    public void addStudent(String n, String g, double p) {
        validate(n, g, p);
        String nameTrim = n.trim();
        String groupTrim = g.trim();

        boolean exists = students.stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(nameTrim) && s.getGroup().equalsIgnoreCase(groupTrim));
        if (exists) {
            throw new IllegalArgumentException("Студент с таким ФИО уже зарегистрирован в этой группе");
        }

        students.add(new Student(nameTrim, groupTrim, p));
        saveToFile();
    }

    public void updateStudent(Student s, String n, String g, double p) {
        if (s == null) throw new IllegalArgumentException("Выберите запись в таблице");
        validate(n, g, p);
        s.setName(n.trim());
        s.setGroup(g.trim());
        s.setGpa(p);
        saveToFile();
    }

    public void deleteStudent(Student s) {
        if (s == null) throw new IllegalArgumentException("Выберите запись в таблице");
        students.remove(s);
        saveToFile();
    }

    public double getAverageGpa() {
        if (students.isEmpty()) return 0.0;
        double sum = 0;
        for (Student s : students) {
            sum += s.getGpa();
        }
        return sum / students.size();
    }

    public double getMaxGpa() {
        if (students.isEmpty()) return 0.0;
        double max = 0.0;
        for (Student s : students) {
            if (s.getGpa() > max) max = s.getGpa();
        }
        return max;
    }

    public int getGroupsCount() {
        HashSet<String> groups = new HashSet<>();
        for (Student s : students) {
            groups.add(s.getGroup());
        }
        return groups.size();
    }

    private void validate(String n, String g, double p) {
        if (n == null || n.isBlank()) throw new IllegalArgumentException("Введите ФИО");
        if (g == null || g.isBlank()) throw new IllegalArgumentException("Укажите группу");
        if (!Double.isFinite(p) || p < 0.0 || p > 4.0)
            throw new IllegalArgumentException("GPA должен быть в диапазоне от 0.0 до 4.0");
    }

    public void saveToFile() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            json.append(String.format("  {\"name\": \"%s\", \"group\": \"%s\", \"gpa\": %.2f}",
                    s.getName().replace("\"", "\\\""),
                    s.getGroup().replace("\"", "\\\""),
                    s.getGpa()).replace(",", "."));
            if (i < students.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]");

        try {
            Files.writeString(Paths.get(FILE_PATH), json.toString());
        } catch (IOException ignored) {}
    }

    private void loadFromFile() {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            students.add(new Student("Сахи Бакберген", "ИС 24-22", 3.5));
            return;
        }

        try {
            String content = Files.readString(path).trim();
            if (content.length() < 5) return;
            content = content.substring(1, content.length() - 1);
            String[] items = content.split("\\},\\s*\\{");
            for (String item : items) {
                String clean = item.replace("{", "").replace("}", "");
                String name = extractJsonVal(clean, "name");
                String group = extractJsonVal(clean, "group");
                double gpa = Double.parseDouble(extractJsonVal(clean, "gpa"));
                students.add(new Student(name, group, gpa));
            }
        } catch (Exception e) {
            students.add(new Student("Сахи Бакберген", "ИС 24-22", 3.5));
        }
    }

    private String extractJsonVal(String json, String key) {
        int keyIdx = json.indexOf("\"" + key + "\"");
        if (keyIdx == -1) return "";
        int colonIdx = json.indexOf(":", keyIdx);
        int valStart = colonIdx + 1;
        while (valStart < json.length() && (json.charAt(valStart) == ' ' || json.charAt(valStart) == '"')) valStart++;
        int valEnd = valStart;
        while (valEnd < json.length() && json.charAt(valEnd) != '"' && json.charAt(valEnd) != ',') valEnd++;
        return json.substring(valStart, valEnd).trim();
    }
}