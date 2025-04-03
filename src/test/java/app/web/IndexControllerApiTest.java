package app.web;

import app.record.service.RecordService;
import app.security.AuthUser;
import app.security.CustomAuthenticationSuccessHandler;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static app.TestBuilder.aRandomUser;
import static app.user.model.Role.USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private RecordService recordService;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        MockHttpServletRequestBuilder request = get("/");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpointWithError_shouldReturnLoginViewWithErrorMessage() throws Exception {
        MockHttpServletRequestBuilder request = get("/login").param("error", "true");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {
        MockHttpServletRequestBuilder request = get("/register");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postRequestToRegisterEndpointWithValidData_shouldRedirectToLogin() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("username", "testuser")
                .param("password", "password123")
                .param("email", "test@example.com")
                .param("phone", "123456789")
                .param("profilePicture", "ww.google.com")
                .param("street", "street123")
                .param("city", "city123")
                .param("state", "state123")
                .param("zip", "123")

                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).registerUser(any());
    }


    @Test
    void getRequestToHomeEndpoint_shouldReturnIndexView() throws Exception {
        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthUser principal = new AuthUser(userId, "User123", "123123", USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attributeExists("user"));
    }


}
