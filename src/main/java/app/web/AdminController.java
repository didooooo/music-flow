package app.web;

import app.artist.model.Artist;
import app.artist.service.ArtistService;
import app.order.model.Order;
import app.order.service.OrderService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.web.dto.RecordUpsertRequest;
import app.web.dto.SearchRecordByNameRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final OrderService orderService;
    private final StatisticService statisticService;
    private final RecordService recordService;
    private final ArtistService artistService;
    public AdminController(OrderService orderService, StatisticService statisticService, RecordService recordService, ArtistService artistService) {
        this.orderService = orderService;
        this.statisticService = statisticService;
        this.recordService = recordService;
        this.artistService = artistService;
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
        modelAndView.setViewName("admin-orders");
        modelAndView.addObject("statistic", statistic);
        modelAndView.addObject("page", "orders");
        modelAndView.addObject("orders", orders);
        return modelAndView;
    }
    @GetMapping("/records")
    public ModelAndView getRecordPage(@RequestParam(defaultValue = "0",name = "page") int page) {
        ModelAndView modelAndView = new ModelAndView();
        Page<Record> records = recordService.getRecordsWithGivenSize(PageRequest.of(page, 8),null);
        modelAndView.setViewName("admin-products");
        List<Artist> artists = artistService.findAll();
        modelAndView.addObject("search", new SearchRecordByNameRequest());
        modelAndView.addObject("records", records);
        modelAndView.addObject("page", "records");
        modelAndView.addObject("recordsRequest", RecordUpsertRequest.builder().artists(artists).build());
        return modelAndView;
    }

}
