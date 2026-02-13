package com.example.project_service.controller;

import com.example.project_service.dto.request.AddWorkspaceMemberRequest;
import com.example.project_service.dto.request.CreateWorkspaceRequest;
import com.example.project_service.dto.request.UpdateWorkspaceRequest;
import com.example.project_service.enums.WorkspaceRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private UUID anotherUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
    }

    @Test
    void createWorkspace_Success() throws Exception {
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
                .name("Engineering")
                .description("Engineering workspace")
                .slug("engineering")
                .build();

        mockMvc.perform(post("/api/workspaces")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Workspace created successfully"))
                .andExpect(jsonPath("$.data.name").value("Engineering"))
                .andExpect(jsonPath("$.data.slug").value("engineering"))
                .andExpect(jsonPath("$.data.memberCount").value(1));
    }

    @Test
    void createWorkspace_DuplicateSlug_Fails() throws Exception {
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
                .name("Engineering")
                .description("Engineering workspace")
                .slug("engineering-dup")
                .build();

        // Create first workspace
        mockMvc.perform(post("/api/workspaces")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to create duplicate
        mockMvc.perform(post("/api/workspaces")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void getUserWorkspaces_Success() throws Exception {
        // Create workspace first
        createTestWorkspace("test-workspace-list");

        mockMvc.perform(get("/api/workspaces")
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getWorkspaceById_Success() throws Exception {
        String workspaceId = createTestWorkspace("test-workspace-get");

        mockMvc.perform(get("/api/workspaces/" + workspaceId)
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(workspaceId));
    }

    @Test
    void getWorkspaceById_NotMember_Fails() throws Exception {
        String workspaceId = createTestWorkspace("test-workspace-access");

        mockMvc.perform(get("/api/workspaces/" + workspaceId)
                        .header("X-User-Id", anotherUserId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void updateWorkspace_Success() throws Exception {
        String workspaceId = createTestWorkspace("test-workspace-update");

        UpdateWorkspaceRequest updateRequest = UpdateWorkspaceRequest.builder()
                .name("Updated Engineering")
                .description("Updated description")
                .build();

        mockMvc.perform(put("/api/workspaces/" + workspaceId)
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Engineering"));
    }

    @Test
    void deleteWorkspace_Success() throws Exception {
        String workspaceId = createTestWorkspace("test-workspace-delete");

        mockMvc.perform(delete("/api/workspaces/" + workspaceId)
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Workspace deleted successfully"));

        // Verify workspace is not accessible
        mockMvc.perform(get("/api/workspaces/" + workspaceId)
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addWorkspaceMember_Success() throws Exception {
        String workspaceId = createTestWorkspace("test-workspace-add-member");

        AddWorkspaceMemberRequest memberRequest = AddWorkspaceMemberRequest.builder()
                .userId(anotherUserId)
                .role(WorkspaceRole.MEMBER)
                .build();

        mockMvc.perform(post("/api/workspaces/" + workspaceId + "/members")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(anotherUserId.toString()))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    @Test
    void getWorkspaceMembers_Success() throws Exception {
        String workspaceId = createTestWorkspace("test-workspace-members");

        mockMvc.perform(get("/api/workspaces/" + workspaceId + "/members")
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].role").value("OWNER"));
    }

    private String createTestWorkspace(String slug) throws Exception {
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
                .name("Test Workspace")
                .description("Test workspace description")
                .slug(slug)
                .build();

        MvcResult result = mockMvc.perform(post("/api/workspaces")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("id").asText();
    }
}
