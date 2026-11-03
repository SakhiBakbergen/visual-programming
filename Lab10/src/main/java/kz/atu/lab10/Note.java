package kz.atu.lab10;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {

    public static final List<String> STATUSES =
            List.of("Хочу посмотреть", "Смотрю", "Просмотрено");

    private String title;
    private String body;
    private String status;

    public Note() {}

    public Note(String title, String body, String status) {
        this.title = title;
        this.body = body;
        this.status = status;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() { return title; }
}