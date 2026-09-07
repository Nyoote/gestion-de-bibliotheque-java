package com.formation.loan.controller;

import com.formation.loan.dto.BookDto;
import com.formation.loan.dto.LoanRequest;
import com.formation.loan.model.Loan;
import com.formation.loan.model.LoanStatus;
import com.formation.loan.repository.LoanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanRepository loanRepository;

    @MockBean
    private com.formation.loan.client.BookClient bookClient;

    @BeforeEach
    void cleanDatabase() {
        loanRepository.deleteAll();
    }

    @Test
    void create_empruntReussi_retourne201() throws Exception {
        BookDto book = new BookDto(3L, "Le Petit Prince", "Saint Exupery", "123456", 5, 5);
        when(bookClient.getBookById(eq(3L))).thenReturn(book);

        LoanRequest request = new LoanRequest("Bob", 3L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberName").value("Bob"))
                .andExpect(jsonPath("$.bookId").value(3))
                .andExpect(jsonPath("$.bookTitle").value("Le Petit Prince"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.returnDate").doesNotExist());
    }

    @Test
    void create_livreInexistant_retourne400() throws Exception {
        when(bookClient.getBookById(eq(999L))).thenThrow(notFoundException());

        LoanRequest request = new LoanRequest("Bob", 999L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le livre 999 demande dans l'emprunt est introuvable"));
    }

    @Test
    void create_stockEpuise_retourne409() throws Exception {
        BookDto book = new BookDto(3L, "Le Petit Prince", "Saint Exupery", "123456", 1, 0);
        when(bookClient.getBookById(eq(3L))).thenReturn(book);

        LoanRequest request = new LoanRequest("Bob", 3L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Aucun exemplaire disponible pour ce livre (id 3)"));
    }

    @Test
    void return_dejaRendu_retourne409() throws Exception {
        Loan loan = new Loan("Bob", 3L, "Le Petit Prince", LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(11), LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now().minusDays(1));
        loan = loanRepository.save(loan);

        mockMvc.perform(patch("/api/loans/{id}/return", loan.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cet emprunt est deja termine (id " + loan.getId() + ")"));
    }

    @Test
    void create_validationInvalide_retourne400() throws Exception {
        LoanRequest request = new LoanRequest("", null);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.memberName").value("Le nom du membre est obligatoire"))
                .andExpect(jsonPath("$.fieldErrors.bookId").value("L'identifiant du livre est obligatoire"));
    }

    private static FeignException.NotFound notFoundException() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.GET,
                "http://book-service/api/books/999", java.util.Map.of(), null,
                feign.Util.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .body(new byte[0])
                .build();
        return (FeignException.NotFound) FeignException.errorStatus("GET /api/books/999", response);
    }
}
