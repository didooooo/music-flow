package app.web;

import app.artist.service.ArtistService;
import app.notification.dto.MessageType;
import app.notification.dto.SendEmailRequest;
import app.notification.service.NotificationService;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.record.service.RecordService;
import app.security.CustomAuthenticationSuccessHandler;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.User;
import app.record.model.Record;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static app.TestBuilder.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
public class ContactControllerApiTest {
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToContact_ShouldReturnContactPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/contact")
                .with(user(aRandomAuthUser()));

        when(userService.getById(any())).thenReturn(User.builder().id(aRandomUser().getId()).build());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeExists("contactRequest", "user"));
    }

    @Test
    void postRequestToContact_ShouldRedirectToContactPage() throws Exception {
        MockHttpServletRequestBuilder request = post("/contact")
                .param("receiver", "test@user.com")
                .param("subject", "test")
                .param("message", "test")
                .param("email", "test@user.com")
                .param("senderEmail", "test@gmail.com")
                .param("senderUsername", "test")
                .with(user(aRandomAuthUser()))
                .with(csrf());

        when(userService.getById(any())).thenReturn(new User());
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/contact"));
    }
}
