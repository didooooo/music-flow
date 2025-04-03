package app.web;

import app.artist.service.ArtistService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerApiTest {
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private StatisticService statisticService;
    @MockitoBean
    private RecordService recordService;
    @MockitoBean
    private ArtistService artistService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToDashboard_shouldReturnTheRequestToDashboard() throws Exception {

        MockHttpServletRequestBuilder request = get("/admin/dashboard")
                .with(user(aRandomAuthAdmin()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(orderService.getLastThreeOrders()).thenReturn(new ArrayList<>());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andExpect(model().attributeExists("statistic", "page", "orders"));
    }

    @Test
    void getRequestToDashboard_shouldReturnException() throws Exception {

        MockHttpServletRequestBuilder request = get("/admin/dashboard")
                .with(user(aRandomAuthUser()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(orderService.getLastThreeOrders()).thenReturn(new ArrayList<>());
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void getRequestToOrders_shouldReturnTheRequestToOrders() throws Exception {

        MockHttpServletRequestBuilder request = get("/admin/orders")
                .with(user(aRandomAuthAdmin()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(orderService.getEightOrders(any())).thenReturn(new PageImpl<>(new ArrayList<>()));
        when(orderService.getTotalQuantityByGivenOrders(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-orders"))
                .andExpect(model().attributeExists("statistic", "page", "orders", "filterRequest", "totalQuantitiesForOrders"));
    }

    @Test
    void getRequestToOrders_shouldReturnException() throws Exception {

        MockHttpServletRequestBuilder request = get("/admin/orders")
                .with(user(aRandomAuthUser()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(orderService.getLastThreeOrders()).thenReturn(new ArrayList<>());
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void getRequestToOrdersFilters_shouldReturnOrdersView() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/orders/filters")
                .param("page", "0")
                .param("productsName", "test")
                .param("orderStatus", OrderStatus.PENDING.name())
                .param("fromDate", LocalDate.now().toString())
                .param("toDate", LocalDate.now().toString())
                .with(user(aRandomAuthAdmin()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(orderService.getEightOrders(any(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
        when(orderService.getTotalQuantityByGivenOrders(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-orders"))
                .andExpect(model().attributeExists("statistic", "page", "orders", "filterRequest", "totalQuantitiesForOrders"));
    }

    @Test
    void getRequestToOrdersFilters_shouldReturnException() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/orders/filters")
                .param("page", "0")
                .param("productsName", "test")
                .param("orderStatus", OrderStatus.PENDING.name())
                .param("fromDate", LocalDate.now().toString())
                .param("toDate", LocalDate.now().toString())
                .with(user(aRandomAuthUser()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(orderService.getEightOrders(any(), any())).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void getRequestToUsers_shouldReturnUsersView() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/users")
                .param("page", "0")
                .with(user(aRandomAuthAdmin()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(userService.getEightUsers(any())).thenReturn(new PageImpl<>(new ArrayList<>()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("statistic", "page", "users", "searchRequest"));
    }

    @Test
    void getRequestToUsers_shouldReturnException() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/users")
                .param("page", "0")
                .with(user(aRandomAuthUser()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(userService.getEightUsers(any())).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void getRequestToUsersWithFilters_shouldReturnUsersView() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/users/filters")
                .param("page", "0")
                .param("searchedName", "test")
                .param("searchedStatus", "active")
                .param("searchedSort", "name")
                .with(user(aRandomAuthAdmin()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(userService.getEightUsers(any(), any())).thenReturn(new PageImpl<>(new ArrayList<>()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("statistic", "page", "users", "searchRequest"));
    }

    @Test
    void getRequestToUsersWithFilters_shouldReturnException() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/users/filters")
                .param("page", "0")
                .param("searchedName", "test")
                .param("searchedStatus", "active")
                .param("searchedSort", "name")
                .with(user(aRandomAuthUser()));

        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(userService.getEightUsers(any(), any())).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void getRequestToEmailPage_shouldReturnEmailView() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/email")
                .param("userEmail", "test@example.com")
                .with(user(aRandomAuthAdmin()));

        when(userService.getAllUsers()).thenReturn(new ArrayList<>());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-send-email"))
                .andExpect(model().attributeExists("page", "users", "sendEmailRequest"))
                .andExpect(model().attribute("sendEmailRequest", hasProperty("receiver", is("test@example.com"))));
    }

    @Test
    void getRequestToEmailPage_shouldReturnException() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/email")
                .with(user(aRandomAuthUser()));

        when(userService.getAllUsers()).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void getRequestToReportsPageWithWeekPeriod_shouldReturnReportsView() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/reports")
                .param("period", "week")
                .with(user(aRandomAuthAdmin()));

        mockCommonReportDependencies("week");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reports"))
                .andExpect(model().attributeExists("topUser", "totalSpentMoneyUser", "topRecord",
                        "period", "totalSoldQuantity", "totalMoneySpent", "page", "chartInfoLabels", "chartInfoValues", "statistics"))
                .andExpect(model().attribute("period", "week"));
    }

    @Test
    void getRequestToReportsPageWithMonthPeriod_shouldReturnReportsView() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/reports")
                .param("period", "month")
                .with(user(aRandomAuthAdmin()));

        mockCommonReportDependencies("month");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reports"))
                .andExpect(model().attributeExists("topUser", "totalSpentMoneyUser", "topRecord",
                        "period", "totalSoldQuantity", "totalMoneySpent", "page", "chartInfoLabels", "chartInfoValues", "statistics"))
                .andExpect(model().attribute("period", "month"));
    }

    @Test
    void getRequestToReportsPageWithYearPeriod_shouldReturnReportsView() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/reports")
                .param("period", "year")
                .with(user(aRandomAuthAdmin()));

        mockCommonReportDependencies("year");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reports"))
                .andExpect(model().attributeExists("topUser", "totalSpentMoneyUser", "topRecord",
                        "period", "totalSoldQuantity", "totalMoneySpent", "page", "chartInfoLabels", "chartInfoValues", "statistics"))
                .andExpect(model().attribute("period", "year"));
    }

    @Test
    void getRequestToReportsPageWithInvalidPeriod_shouldDefaultToWeek() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/reports")
                .param("period", "invalid")
                .with(user(aRandomAuthAdmin()));

        mockCommonReportDependencies("week");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-reports"))
                .andExpect(model().attributeExists("topUser", "totalSpentMoneyUser", "topRecord",
                        "period", "totalSoldQuantity", "totalMoneySpent", "page", "chartInfoLabels", "chartInfoValues", "statistics"))
                .andExpect(model().attribute("period", "invalid"));
    }

    private void mockCommonReportDependencies(String period) {
        when(statisticService.getStatisticsForToday()).thenReturn(new Statistics());
        when(recordService.getTopSellingRecord()).thenReturn(new Record());
        when(userService.getTopCustomer()).thenReturn(User.builder().orders(new ArrayList<>(List.of(new Order()))).build());
        when(recordService.getTotalSoldQuantityAndTotalMoneySpent(any())).thenReturn(Pair.of(BigDecimal.TEN, 100));
        when(userService.getTotalSpentMoneyByGivenUser(any())).thenReturn(BigDecimal.TEN);
        when(orderService.getChartInfo(period)).thenReturn(Map.of("Day1", 5, "Day2", 10));
    }


    @Test
    void getRequestToRecords_shouldReturnTheRecordPage() throws Exception {

        MockHttpServletRequestBuilder request = get("/admin/records")
                .with(user(aRandomAuthAdmin()));

        when(recordService.getRecordsWithGivenSize(any(PageRequest.class), any())).thenReturn(new PageImpl<>(new ArrayList<>()));
        when(artistService.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-products"))
                .andExpect(model().attributeExists("search", "records", "page", "recordsRequest"));
    }

    @Test
    void postRequestToSendEmail_shouldSendEmailAndRedirect() throws Exception {

        MockHttpServletRequestBuilder request = post("/admin/email")
                .param("receiver", "test@user.com")
                .param("subject", "test")
                .param("message", "test")
                .param("email", "test@user.com")
                .param("senderEmail", "test@gmail.com")
                .param("senderUsername", "test")
                .with(user(aRandomAuthAdmin()))
                .with(csrf());

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("admin");

        when(userService.getUserByEmail(anyString())).thenReturn(user);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/email"));

        verify(notificationService, times(1)).createNotificationFromAdminToUser(any(SendEmailRequest.class));
    }
    @Test
    void getRequestToNotifications_shouldRedirectToNotificationPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/admin/notifications")
                .with(user(aRandomAuthAdmin()));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/notifications"));
    }


}
