package com.example.sambookcatalogue;

import java.util.Objects;

public class Book {
    private String title;
    private String author;
    private int coverResourceId;
    private String description;
    private double price;

    public Book(String title, String author, int coverResourceId, String description, double price) {
        this.title = title;
        this.author = author;
        this.coverResourceId = coverResourceId;
        this.description = description;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getCoverResourceId() {
        return coverResourceId;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(title, book.title) && Objects.equals(author, book.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author);
    }
}