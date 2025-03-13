package app.web;

import app.security.AuthUser;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ModelAndView getProfilePage(@RequestParam("username") String username,@AuthenticationPrincipal AuthUser authUser) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        modelAndView.addObject("user", user);
        modelAndView.setViewName("profile");
        return modelAndView;
    }
}
