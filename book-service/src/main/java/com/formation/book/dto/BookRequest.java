package com.formation.book.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookRequest {

    @NotBlank(message = "Le nom du livre est obligatoire")
    private String title;

    @NotBlank(message = "Le nom de l'auteur est obligatoire")
    private String author;

    @NotBlank(message = "L'isbn est obligatoire")
    private String isbn;

    @NotNull(message = "Le nombre de copies est obligatoire")
    @Min(value = 1, message = "Le nombre de copies doit etre superieur ou égal à 1")
    private Integer totalCopies;

    @NotNull(message = "La quantite en stock est obligatoire")
    @Min(value = 0, message = "La quantite en stock doit etre positive ou nulle")
    private Integer availableCopies;

    @AssertTrue(message = "La quantite en stock ne peut pas depasser le nombre total de copies")
    public boolean isAvailableCopiesValid() {
        return totalCopies == null || availableCopies == null || availableCopies <= totalCopies;
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

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}