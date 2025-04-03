package app.web;

import app.artist.service.ArtistService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.review.service.ReviewService;
import app.security.CustomAuthenticationSuccessHandler;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static app.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordController.class)
public class RecordControllerApiTest {
    @MockitoBean
    private RecordService recordService;
    @MockitoBean
    private ArtistService artistService;
    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllRecords_shouldReturnAllRecordsView() throws Exception {
        MockHttpServletRequestBuilder request = get("/records/all")
                .with(user(aRandomAuthUser()))
                .param("page", "0")
                .param("sort", "name");

        when(recordService.getRecordsWithGivenSize(any(), anyString())).thenReturn(new PageImpl<>(new ArrayList<>()));
        when(reviewService.getRatingByGivenRecords(anyList())).thenReturn(new ArrayList<>());
        when(userService.getById(any(UUID.class))).thenReturn(new User());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("records", "user", "ratings", "resultName", "sort"));
    }

    @Test
    void getAllWithFilter_shouldReturnFilteredRecordsView() throws Exception {
        MockHttpServletRequestBuilder request = get("/records/filter")
                .with(user(aRandomAuthUser()))
                .param("page", "0")
                .param("format", "CD", "Vinyl")
                .param("genre", "Rock")
                .param("query", "name")
                .param("media", "LP")
                .param("sort", "name")
                .param("minPrice", "10")
                .param("maxPrice", "50");

        when(recordService.getEightRecordsWithFilterParameters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(new ArrayList<>(List.of(Record.builder().build()))));
        when(reviewService.getRatingByGivenRecords(any())).thenReturn(new ArrayList<>(List.of(1,2,3)));
        when(userService.getById(any(UUID.class))).thenReturn(aRandomUser());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("records", "user", "ratings", "resultName","sort","query",
                        "selectedFormats", "selectedGenres", "selectedTypes", "minPrice", "maxPrice"));
    }

    @Test
    void getAllWithFilter_withNoParams_shouldRedirect() throws Exception {
        MockHttpServletRequestBuilder request = get("/records/filter")
                .with(user(aRandomAuthUser()))
                .param("page", "0");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/records/all*"));
    }

    @Test
    void saveRecord_shouldSaveAndRedirect() throws Exception {
        MockHttpServletRequestBuilder request = post("/records/save")
                .with(user(aRandomAuthAdmin()))
                .param("title", "Test Album")
                .param("price", "19.99")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/records"));
    }

    @Test
    void updateRecord_shouldUpdateAndRedirect() throws Exception {
        UUID recordId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = put("/records/save/" + recordId)
                .with(user(aRandomAuthAdmin()))
                .param("title", "Updated Album")
                .param("price", "25.99")
                .param("page", "0")
                .with(csrf());

        when(recordService.getRecordsWithGivenSize(any(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
        when(artistService.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/records"));
    }

    @Test
    void getSearchedRecordsByName_shouldReturnAdminProductsView() throws Exception {
        MockHttpServletRequestBuilder request = get("/records/search")
                .with(user(aRandomAuthAdmin()))
                .param("name", "test")
                .param("page", "0");

        when(recordService.getEightRecordsByName(any(), anyString())).thenReturn(new PageImpl<>(new ArrayList<>()));
        when(artistService.findAll()).thenReturn(new ArrayList<>());
        when(userService.getById(any(UUID.class))).thenReturn(new User());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-products"))
                .andExpect(model().attributeExists("records", "user", "page", "search", "recordsRequest"));
    }


    @Test
    void deleteRecord_shouldDeleteAndRedirect() throws Exception {
        UUID recordId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = delete("/records/%s".formatted(recordId.toString()))
                .with(user(aRandomAuthAdmin()))
                .with(csrf());

        when(recordService.findById(any())).thenReturn(new Record());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/records"));
    }
}
