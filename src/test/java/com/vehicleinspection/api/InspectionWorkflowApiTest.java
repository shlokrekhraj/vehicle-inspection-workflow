package com.vehicleinspection.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Fast API-level tests of the whole MVP workflow (runs on every build, H2 database). */
@SpringBootTest
@AutoConfigureMockMvc
class InspectionWorkflowApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private long submit(String vehicleNumber) throws Exception {
        String json = """
                {"vehicleNumber":"%s","vehicleType":"Car","ownerName":"Test Owner",
                 "documentName":"Registration Certificate"}""".formatted(vehicleNumber);
        String body = mvc.perform(post("/api/inspection-requests")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(body);
        return node.get("id").asLong();
    }

    @Test
    void registersAndLogsInWithHashedPassword() throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Asha\",\"email\":\"Asha@Example.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("asha@example.com"))
                .andExpect(jsonPath("$.role").value("APPLICANT"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"asha@example.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"asha@example.com\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsDuplicateEmailAndBadInput() throws Exception {
        String user = "{\"name\":\"Ravi\",\"email\":\"ravi@example.com\",\"password\":\"secret123\"}";
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(user))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(user))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\",\"password\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    void validatesInspectionRequest() throws Exception {
        mvc.perform(post("/api/inspection-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicleNumber\":\"123\",\"vehicleType\":\"Plane\",\"ownerName\":\"\",\"documentName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(4)));
    }

    @Test
    void approvesRequestThroughFullWorkflow() throws Exception {
        long id = submit("mh 02 ab 1234");

        mvc.perform(get("/api/inspection-requests").param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        mvc.perform(put("/api/inspection-requests/{id}/decision", id).param("decision", "APPROVE"))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/inspection-requests/{id}/review", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        mvc.perform(put("/api/inspection-requests/{id}/decision", id)
                        .param("decision", "APPROVE").param("remarks", "All documents valid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mvc.perform(get("/api/inspection-requests/{id}/status", id))
                .andExpect(jsonPath("$.vehicleNumber").value("MH02AB1234"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.remarks").value("All documents valid"));
    }

    @Test
    void rejectionRequiresRemarks() throws Exception {
        long id = submit("KA05MN4321");
        mvc.perform(put("/api/inspection-requests/{id}/review", id)).andExpect(status().isOk());

        mvc.perform(put("/api/inspection-requests/{id}/decision", id).param("decision", "REJECT"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/inspection-requests/{id}/decision", id)
                        .param("decision", "REJECT").param("remarks", "Insurance expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void unknownRequestReturns404AndHealthIsUp() throws Exception {
        mvc.perform(get("/api/inspection-requests/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Inspection request not found"));

        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
