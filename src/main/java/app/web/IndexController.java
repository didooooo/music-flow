package app.web;

import app.order.model.Order;
import app.order.service.OrderService;
import app.security.AuthUser;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.Role;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class IndexController {
    private final UserService userService;
    private final StatisticService statisticService;
    private final OrderService orderService;

    public IndexController(UserService userService, StatisticService statisticService, OrderService orderService) {
        this.userService = userService;
        this.statisticService = statisticService;
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password!");
        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.registerUser(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthUser authUser) {

        User user = userService.getById(authUser.getUserId());

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("index");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

}
