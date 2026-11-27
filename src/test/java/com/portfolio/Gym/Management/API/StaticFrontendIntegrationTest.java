package com.portfolio.Gym.Management.API;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StaticFrontendIntegrationTest {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesIndexPage() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gym Management")));
    }

    @Test
    void servesStaticAssets() throws Exception {
        mockMvc.perform(get("/css/styles.css")).andExpect(status().isOk());
        mockMvc.perform(get("/js/app.js")).andExpect(status().isOk());
    }

    @Test
    void protectsApiEndpoints() throws Exception {
        mockMvc.perform(get("/members")).andExpect(status().isUnauthorized());
    }

    @Test
    void registersUserWithUserRole() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"frontend@teste.com\",\"password\":\"senha123\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"role\":\"USER\"")));
    }

    @Test
    void regularUserCannotCreateMembers() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"readonly@teste.com\",\"password\":\"senha123\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        Matcher matcher = TOKEN_PATTERN.matcher(result.getResponse().getContentAsString());
        assertTrue(matcher.find(), "token should be present");
        String token = matcher.group(1);

        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Teste\",\"email\":\"aluno@teste.com\",\"phone\":\"11999999999\"}"))
                .andExpect(status().isForbidden());
    }
}

