package kz.atu.lab11;

import kz.atu.lab11.service.StudentService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StudentServiceTest {

    @Test
    void shouldAddStudent() {
        StudentService s = new StudentService();
        s.addStudent("Алия", "ИС-301", 3.7);
        assertEquals(1, s.getStudents().size());
    }

    @Test
    void shouldRejectInvalidGpa() {
        StudentService s = new StudentService();
        assertThrows(IllegalArgumentException.class,
                () -> s.addStudent("Алия", "ИС-301", 5.0));
    }

    @Test
    void shouldRejectDuplicateStudent() {
        StudentService s = new StudentService();
        s.addStudent("Алия", "ИС-301", 3.7);
        assertThrows(IllegalArgumentException.class,
                () -> s.addStudent("Алия", "ИС-301", 3.9));
    }

    @Test
    void shouldAcceptBoundaryGpaValues() {
        StudentService s = new StudentService();
        assertDoesNotThrow(() -> s.addStudent("Тест Ноль", "ИС-301", 0.0));
        assertDoesNotThrow(() -> s.addStudent("Тест Четыре", "ИС-301", 4.0));
    }

    @Test
    void shouldCalculateAverageGpa() {
        StudentService s = new StudentService();
        s.getStudents().clear();
        s.addStudent("Студент 1", "ИС-1", 2.0);
        s.addStudent("Студент 2", "ИС-1", 4.0);
        assertEquals(3.0, s.getAverageGpa(), 0.001);
    }
}
