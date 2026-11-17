package kz.atu.lab12.model;

/** Неизменяемая модель строки таблицы books. */
public record Book(int id, String title, String author, int year) {}
