package com.formation.book.dto;

// dto/BookResponse.java — ce que l'API RENVOIE (inclut l'id, contrairement au Request)
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer totalCopies;
    private Integer availableCopies;

    public BookResponse(Long id, String title, String author, String isbn, Integer totalCopies,
            Integer availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }
}