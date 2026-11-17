package kz.atu.lab12.repository;

import kz.atu.lab12.model.Book;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Весь доступ к SQLite сосредоточен здесь: контроллер SQL не содержит. */
public class BookRepository {

    private static final String URL = "jdbc:sqlite:books.db";

    /** Создаёт таблицу, если её ещё нет (файл books.db создаётся при подключении). */
    public void initializeDatabase() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS books (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    title       TEXT    NOT NULL,
                    author      TEXT    NOT NULL,
                    book_year   INTEGER NOT NULL CHECK (book_year BETWEEN 1 AND 2100)
                )
                """;
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }

    /** READ: все записи. */
    public List<Book> findAll() throws SQLException {
        List<Book> result = new ArrayList<>();
        String sql = "SELECT id, title, author, book_year FROM books ORDER BY id";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("book_year")));
            }
        }
        return result;
    }

    /** CREATE. */
    public void insert(String title, String author, int year) throws SQLException {
        String sql = "INSERT INTO books(title, author, book_year) VALUES (?, ?, ?)";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, year);
            ps.executeUpdate();
        }
    }

    /** UPDATE. Возвращает число изменённых строк (0 — записи с таким id нет). */
    public int update(int id, String title, String author, int year) throws SQLException {
        String sql = "UPDATE books SET title=?, author=?, book_year=? WHERE id=?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, year);
            ps.setInt(4, id);
            return ps.executeUpdate();
        }
    }

    /** DELETE. Возвращает число удалённых строк. */
    public int delete(int id) throws SQLException {
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement("DELETE FROM books WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
