package com.example.analytics_svc.web;

import com.example.analytics_svc.model.ProjectAnalytics;
import com.example.analytics_svc.repository.ProjectAnalyticsRepository;
import com.example.analytics_svc.repository.ProjectSnapshotRepository;
import com.example.analytics_svc.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectSnapshotController.class)
public class ProjectAnalyticsApiTest {

    @MockitoBean
    private ProjectService projectService;
    @MockitoBean
    private ProjectSnapshotRepository snapshotRepository;
    @MockitoBean
    private ProjectAnalyticsRepository analyticsRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postUpdateAnalytics_withEmptyRequests_shouldEmptyAnalytics() throws Exception {
        UUID userId = UUID.randomUUID();

        ProjectAnalytics emptyProjectAnalytics = new ProjectAnalytics();

        when(projectService.emptyAnalytics(userId)).thenReturn(emptyProjectAnalytics);

        MockHttpServletRequestBuilder httpRequest = post("/api/v1/projects/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]");

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk());
    }

    @Test
    void postUpdateAnalytics_withActualRequests_shouldUpdateAnalytics() throws Exception {
        UUID userId = UUID.randomUUID();

        ProjectAnalytics projectAnalytics = ProjectAnalytics.builder()
                .userId(userId)
                .completedProjects(2)
                .totalProjects(5)
                .build();

        when(projectService.upsertProjects(any(), eq(userId))).thenReturn(projectAnalytics);

        MockHttpServletRequestBuilder httpRequest = post("/api/v1/projects/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            [
                              { "status": "DONE", "priority": "LOW" }
                            ]
                        """);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.completedProjects").value(2))
                .andExpect(jsonPath("$.totalProjects").value(5));
    }

    @Test
    void getProjectAnalytics_withEmptyAnalytics_shouldEmptyAnalytics() throws Exception {
        UUID userId = UUID.randomUUID();
        ProjectAnalytics empty = new ProjectAnalytics();

        when(projectService.emptyAnalytics(userId)).thenReturn(empty);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/projects/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]");

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk());

        verify(projectService).emptyAnalytics(userId);
        verify(analyticsRepository).save(empty);
    }

}
