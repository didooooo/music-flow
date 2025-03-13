package app.web;

import app.order.model.Order;
import app.order.service.OrderService;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final OrderService orderService;
    private final StatisticService statisticService;

    public AdminController(OrderService orderService, StatisticService statisticService) {
        this.orderService = orderService;
        this.statisticService = statisticService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getAdminDashboard() {
        Statistics statistic = statisticService.getStatisticsForToday();
        ModelAndView modelAndView = new ModelAndView();
        List<Order> orders = orderService.getLastThreeOrders();
        modelAndView.setViewName("admin-dashboard");
        modelAndView.addObject("statistic", statistic);
        modelAndView.addObject("orders", orders);
        return modelAndView;
    }
}
