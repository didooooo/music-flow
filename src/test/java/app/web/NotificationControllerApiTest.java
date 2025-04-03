package app.web;

import app.artist.service.ArtistService;
import app.notification.dto.Notification;
import app.notification.dto.SendEmailRequest;
import app.notification.service.NotificationService;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.security.CustomAuthenticationSuccessHandler;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.User;
import app.user.service.UserService;
import org.checkerframework.checker.units.qual.N;
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

import static app.TestBuilder.aRandomAuthAdmin;
import static app.TestBuilder.aRandomAuthUser;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToAllNotifications_shouldReturnNotificationsPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/notifications")
                .with(user(aRandomAuthAdmin()));

        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification());
        List<String> prettyDateFormat = new ArrayList<>();
        prettyDateFormat.add("just now");

        when(notificationService.getAll()).thenReturn(notifications);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-notification"))
                .andExpect(model().attributeExists("notifications", "page", "criteria", "prettyDateFormat"))
                .andExpect(model().attribute("criteria", "all"));
    }

    @Test
    void getRequestToReadNotifications_shouldReturnReadNotificationsPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/notifications/read")
                .with(user(aRandomAuthAdmin()));
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification());
        List<String> prettyDateFormat = new ArrayList<>();
        prettyDateFormat.add("just now");

        when(notificationService.getAllRead(true)).thenReturn(notifications);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-notification"))
                .andExpect(model().attributeExists("notifications", "page", "criteria", "prettyDateFormat"))
                .andExpect(model().attribute("criteria", "read"));
    }
    @Test
    void getRequestToUnreadNotifications_shouldReturnUnreadNotificationsPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/notifications/unread")
                .with(user(aRandomAuthAdmin()));

        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification());
        List<String> prettyDateFormat = new ArrayList<>();
        prettyDateFormat.add("just now");

        when(notificationService.getAllRead(false)).thenReturn(notifications);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-notification"))
                .andExpect(model().attributeExists("notifications", "page", "criteria", "prettyDateFormat"))
                .andExpect(model().attribute("criteria", "unread"));
    }

    @Test
    void deleteRequestToNotification_shouldRedirectToNotificationsPage() throws Exception {
        UUID notificationId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = delete("/notifications/{id}", notificationId)
                .with(user(aRandomAuthAdmin()))
                .with(csrf());

        doNothing().when(notificationService).deleteNotificationById(notificationId);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())  // Очакваме пренасочване
                .andExpect(view().name("redirect:/notifications"));

        verify(notificationService, times(1)).deleteNotificationById(notificationId);
    }
    @Test
    void putRequestToUpdateNotification_shouldRedirectToNotificationsPage() throws Exception {
        UUID notificationId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = put("/notifications/{id}", notificationId)
                .with(user(aRandomAuthAdmin()))
                .with(csrf());
        doNothing().when(notificationService).updateNotification(notificationId);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())  // Очакваме пренасочване
                .andExpect(view().name("redirect:/notifications"));

        verify(notificationService, times(1)).updateNotification(notificationId);
    }

}
