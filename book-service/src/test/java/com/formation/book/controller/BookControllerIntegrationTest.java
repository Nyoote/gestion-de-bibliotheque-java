package com.formation.book.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cycleDeVieComplet() throws Exception {
        long id = createBook("Dune", "Frank Herbert", "9780441013593", 3, 2);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Dune")));
        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"));
        mockMvc.perform(put("/api/books/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson("Dune modifie", "Frank Herbert", "9780441013593", 3, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune modifie"));
        mockMvc.perform(delete("/api/books/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void decrementStockEpuiseRetourne409() throws Exception {
        long id = createBook("Livre vide", "Auteur", "ISBN-EMPTY", 1, 0);

        mockMvc.perform(post("/api/books/{id}/decrement-stock", id))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/books/{id}/decrement-stock", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void incrementStockAuDelaDuTotalRetourne409() throws Exception {
        long id = createBook("Livre plein", "Auteur", "ISBN-FULL", 1, 1);

        mockMvc.perform(post("/api/books/{id}/increment-stock", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void livreInexistantRetourne404() throws Exception {
        mockMvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationInvalideRetourne400() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson("", "Auteur", "ISBN", 0, 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private long createBook(String title, String author, String isbn, int totalCopies, int availableCopies)
            throws Exception {
        MvcResult result = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson(title, author, isbn, totalCopies, availableCopies)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private String bookJson(String title, String author, String isbn, int totalCopies, int availableCopies)
            throws Exception {
        return objectMapper.createObjectNode()
                .put("title", title)
                .put("author", author)
                .put("isbn", isbn)
                .put("totalCopies", totalCopies)
                .put("availableCopies", availableCopies)
                .toString();
    }
}