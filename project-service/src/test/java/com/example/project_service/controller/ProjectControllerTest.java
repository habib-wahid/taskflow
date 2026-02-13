package com.example.project_service.controller;

import com.example.project_service.dto.request.AddProjectMemberRequest;
import com.example.project_service.dto.request.CreateProjectRequest;
import com.example.project_service.dto.request.CreateWorkspaceRequest;
import com.example.project_service.dto.request.UpdateProjectRequest;
import com.example.project_service.dto.request.AddWorkspaceMemberRequest;
import com.example.project_service.enums.ProjectRole;
import com.example.project_service.enums.ProjectStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private UUID anotherUserId;
    private String workspaceId;

    @BeforeEach
    void setUp() throws Exception {
        testUserId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        workspaceId = createTestWorkspace();
    }

    @Test
    void createProject_Success() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .workspaceId(UUID.fromString(workspaceId))
                .name("Payment Integration")
                .description("Integrate Stripe")
                .key("PAY")
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 4, 1))
                .budget(new BigDecimal("10000"))
                .build();

        mockMvc.perform(post("/api/projects")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project created successfully"))
                .andExpect(jsonPath("$.data.name").value("Payment Integration"))
                .andExpect(jsonPath("$.data.key").value("PAY"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.memberCount").value(1));
    }

    @Test
    void createProject_DuplicateKey_Fails() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .workspaceId(UUID.fromString(workspaceId))
                .name("Project 1")
                .key("DUP")
                .build();

        // Create first project
        mockMvc.perform(post("/api/projects")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to create with same key
        request.setName("Project 2");
        mockMvc.perform(post("/api/projects")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void createProject_NotWorkspaceMember_Fails() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .workspaceId(UUID.fromString(workspaceId))
                .name("Unauthorized Project")
                .key("UNAUTH")
                .build();

        mockMvc.perform(post("/api/projects")
                        .header("X-User-Id", anotherUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void getUserProjects_Success() throws Exception {
        createTestProject("TST");

        mockMvc.perform(get("/api/projects")
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getUserProjects_WithWorkspaceFilter_Success() throws Exception {
        createTestProject("TST2");

        mockMvc.perform(get("/api/projects")
                        .header("X-User-Id", testUserId.toString())
                        .param("workspaceId", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getProjectById_Success() throws Exception {
        String projectId = createTestProject("GET");

        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(projectId));
    }

    @Test
    void updateProject_Success() throws Exception {
        String projectId = createTestProject("UPD");

        UpdateProjectRequest updateRequest = UpdateProjectRequest.builder()
                .name("Updated Project")
                .description("Updated description")
                .status(ProjectStatus.ON_HOLD)
                .build();

        mockMvc.perform(put("/api/projects/" + projectId)
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Project"))
                .andExpect(jsonPath("$.data.status").value("ON_HOLD"));
    }

    @Test
    void archiveProject_Success() throws Exception {
        String projectId = createTestProject("ARC");

        mockMvc.perform(patch("/api/projects/" + projectId + "/archive")
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }

    @Test
    void getProjectAnalytics_Success() throws Exception {
        String projectId = createTestProject("ANA");

        mockMvc.perform(get("/api/projects/" + projectId + "/analytics")
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.totalMembers").value(1));
    }

    @Test
    void addProjectMember_Success() throws Exception {
        String projectId = createTestProject("MBR");

        // First add user to workspace
        AddWorkspaceMemberRequest workspaceMemberRequest = AddWorkspaceMemberRequest.builder()
                .userId(anotherUserId)
                .role(WorkspaceRole.MEMBER)
                .build();

        mockMvc.perform(post("/api/workspaces/" + workspaceId + "/members")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workspaceMemberRequest)))
                .andExpect(status().isCreated());

        // Then add to project
        AddProjectMemberRequest memberRequest = AddProjectMemberRequest.builder()
                .userId(anotherUserId)
                .role(ProjectRole.MEMBER)
                .build();

        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(anotherUserId.toString()))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    @Test
    void addProjectMember_NotWorkspaceMember_Fails() throws Exception {
        String projectId = createTestProject("MBR2");

        AddProjectMemberRequest memberRequest = AddProjectMemberRequest.builder()
                .userId(anotherUserId)
                .role(ProjectRole.MEMBER)
                .build();

        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }

    @Test
    void getProjectMembers_Success() throws Exception {
        String projectId = createTestProject("GMP");

        mockMvc.perform(get("/api/projects/" + projectId + "/members")
                        .header("X-User-Id", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].role").value("OWNER"));
    }

    private String createTestWorkspace() throws Exception {
        CreateWorkspaceRequest request = CreateWorkspaceRequest.builder()
                .name("Test Workspace")
                .description("Test workspace description")
                .slug("test-workspace-" + UUID.randomUUID().toString().substring(0, 8))
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

    private String createTestProject(String key) throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .workspaceId(UUID.fromString(workspaceId))
                .name("Test Project " + key)
                .description("Test project description")
                .key(key)
                .build();

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .header("X-User-Id", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("id").asText();
    }
}
