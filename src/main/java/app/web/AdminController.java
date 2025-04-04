package app.web;

import app.artist.model.Artist;
import app.artist.service.ArtistService;
import app.notification.dto.SendEmailRequest;
import app.notification.service.NotificationService;
import app.order.model.Order;
import app.order.service.OrderService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.OrderFilterRequest;
import app.web.dto.RecordUpsertRequest;
import app.web.dto.SearchRecordByNameRequest;
import app.web.dto.UserFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final OrderService orderService;
    private final StatisticService statisticService;
    private final RecordService recordService;
    private final ArtistService artistService;
    private final UserService userService;
    private final NotificationService notificationService;

    public AdminController(OrderService orderService, StatisticService statisticService, RecordService recordService, ArtistService artistService, UserService userService, NotificationService notificationService) {
        this.orderService = orderService;
        this.statisticService = statisticService;
        this.recordService = recordService;
        this.artistService = artistService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getAdminDashboard() {
        Statistics statistic = statisticService.getStatisticsForToday();
        ModelAndView modelAndView = new ModelAndView();
        List<Order> orders = orderService.getLastThreeOrders();
        modelAndView.setViewName("admin-dashboard");
        modelAndView.addObject("statistic", statistic);
        modelAndView.addObject("page", "dashboard");
        modelAndView.addObject("orders", orders);
        return modelAndView;
    }

    @GetMapping("/orders")
    public ModelAndView getOrdersPage(@RequestParam(defaultValue = "0") int page) {
        Statistics statistic = statisticService.getStatisticsForToday();
        ModelAndView modelAndView = new ModelAndView();
        Page<Order> orders = orderService.getEightOrders(PageRequest.of(page, 8));
        List<Integer> totalQuantitiesForOrders = orderService.getTotalQuantityByGivenOrders(orders.getContent());
        modelAndView.setViewName("admin-orders");
        modelAndView.addObject("statistic", statistic);
        modelAndView.addObject("page", "orders");
        modelAndView.addObject("orders", orders);
        modelAndView.addObject("totalQuantitiesForOrders", totalQuantitiesForOrders);
        modelAndView.addObject("filterRequest", new OrderFilterRequest());
        return modelAndView;
    }

    @GetMapping("/orders/filters")
    public ModelAndView getOrdersPageWithFilters(@RequestParam(defaultValue = "0", name = "page") int page,
                                                 OrderFilterRequest orderFilterRequest) {
        ModelAndView modelAndView = new ModelAndView();
        Statistics statistics = statisticService.getStatisticsForToday();
        Page<Order> orders = orderService.getEightOrders(PageRequest.of(page, 8), orderFilterRequest);
        List<Integer> totalQuantitiesForOrders = orderService.getTotalQuantityByGivenOrders(orders.getContent());
        modelAndView.setViewName("admin-orders");
        modelAndView.addObject("page", "orders");
        modelAndView.addObject("orders", orders);
        modelAndView.addObject("statistic", statistics);
        modelAndView.addObject("totalQuantitiesForOrders", totalQuantitiesForOrders);
        modelAndView.addObject("filterRequest", orderFilterRequest);
        return modelAndView;
    }

    @GetMapping("/records")
    public ModelAndView getRecordPage(@RequestParam(defaultValue = "0", name = "page") int page) {
        ModelAndView modelAndView = new ModelAndView();
        Page<Record> records = recordService.getRecordsWithGivenSize(PageRequest.of(page, 8), null);
        modelAndView.setViewName("admin-products");
        List<Artist> artists = artistService.findAll();
        modelAndView.addObject("search", new SearchRecordByNameRequest());
        modelAndView.addObject("records", records);
        modelAndView.addObject("page", "records");
        modelAndView.addObject("recordsRequest", RecordUpsertRequest.builder().artists(artists).build());
        return modelAndView;
    }

    @GetMapping("/users")
    public ModelAndView getUsersPage(@RequestParam(defaultValue = "0", name = "page") int page) {
        ModelAndView modelAndView = new ModelAndView();
        Page<User> users = userService.getEightUsers(PageRequest.of(page, 8));
        Statistics statistics = statisticService.getStatisticsForToday();
        modelAndView.setViewName("admin-users");
        modelAndView.addObject("page", "users");
        modelAndView.addObject("users", users);
        modelAndView.addObject("statistic", statistics);
        modelAndView.addObject("searchRequest", new UserFilterRequest());
        return modelAndView;
    }

    @GetMapping("/users/filters")
    public ModelAndView getUsersPageWithFilters(@RequestParam(defaultValue = "0", name = "page") int page,
                                                UserFilterRequest userFilterRequest) {
        ModelAndView modelAndView = new ModelAndView();
        Statistics statistics = statisticService.getStatisticsForToday();
        Page<User> users = userService.getEightUsers(PageRequest.of(page, 8), userFilterRequest);
        modelAndView.setViewName("admin-users");
        modelAndView.addObject("page", "users");
        modelAndView.addObject("users", users);
        modelAndView.addObject("statistic", statistics);
        modelAndView.addObject("searchRequest", userFilterRequest);
        return modelAndView;
    }

    @GetMapping("/reports")
    public ModelAndView getReportsPage(@RequestParam(name = "period", defaultValue = "week") String period) {
        ModelAndView modelAndView = new ModelAndView();
        Statistics statisticsForToday = statisticService.getStatisticsForToday();
        Record record = recordService.getTopSellingRecord();
        User user = userService.getTopCustomer();
        Pair<BigDecimal, Integer> totalSoldQuantityAndTotalMoneySpent = recordService.getTotalSoldQuantityAndTotalMoneySpent(record);
        BigDecimal totalSpentMoney = userService.getTotalSpentMoneyByGivenUser(user);
        Map<String, Integer> chartInfo = orderService.getChartInfo(period);
        modelAndView.setViewName("admin-reports");
        modelAndView.addObject("topUser", user);
        modelAndView.addObject("totalSpentMoneyUser", totalSpentMoney);
        modelAndView.addObject("topRecord", record);
        modelAndView.addObject("period", period);
        modelAndView.addObject("totalSoldQuantity", totalSoldQuantityAndTotalMoneySpent.getSecond());
        modelAndView.addObject("totalMoneySpent", totalSoldQuantityAndTotalMoneySpent.getFirst());
        modelAndView.addObject("page", "reports");
        if (period.equals("week")) {
            modelAndView.addObject("chartInfoLabels", new ArrayList<>(chartInfo.keySet()).reversed());
            modelAndView.addObject("chartInfoValues", new ArrayList<>(chartInfo.values()).reversed());
        } else {
            modelAndView.addObject("chartInfoLabels", new ArrayList<>(chartInfo.keySet()));
            modelAndView.addObject("chartInfoValues", new ArrayList<>(chartInfo.values()));
        }
        modelAndView.addObject("statistics", statisticsForToday);
        return modelAndView;
    }

    @GetMapping("/email")
    public ModelAndView getEmailPage(@RequestParam(name = "userEmail",required = false) String email) {
        ModelAndView modelAndView = new ModelAndView();
        List<User> users = userService.getAllUsers();
        modelAndView.setViewName("admin-send-email");
        modelAndView.addObject("page", "email");
        modelAndView.addObject("users", users);
        modelAndView.addObject("sendEmailRequest", SendEmailRequest.builder().receiver(email).build());
        return modelAndView;
    }
    @PostMapping("/email")
    public ModelAndView sendEmail(SendEmailRequest emailRequest) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUserByEmail(emailRequest.getReceiver());
        emailRequest.setUserId(user.getId());
        emailRequest.setSenderUsername(user.getUsername());
        notificationService.createNotificationFromAdminToUser(emailRequest);
        modelAndView.setViewName("redirect:/admin/email");
        return modelAndView;
    }
    @GetMapping("/notifications")
    public ModelAndView getNotificationsPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/notifications");
        return modelAndView;
    }

}
