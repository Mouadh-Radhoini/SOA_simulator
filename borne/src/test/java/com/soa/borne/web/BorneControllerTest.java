package com.soa.borne.web;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneType;
import com.soa.borne.infrastructure.persistence.BorneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BorneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BorneRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAndFlush(new Borne("api-borne", BorneType.ELECTRICITY, 7.2));
    }

    @Test
    void returnsBorneAsDto() throws Exception {
        mockMvc.perform(get("/api/bornes/api-borne"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("api-borne")))
                .andExpect(jsonPath("$.status", is("AVAILABLE")));
    }

    @Test
    void returnsConflictForInvalidTransition() throws Exception {
        mockMvc.perform(post("/api/bornes/api-borne/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void returnsNotFoundForUnknownBorne() throws Exception {
        mockMvc.perform(get("/api/bornes/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
