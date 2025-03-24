package app.web;

import app.security.AuthUser;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ModelAndView getProfilePage(@RequestParam("username") String username, @AuthenticationPrincipal AuthUser authUser) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        modelAndView.addObject("user", user);
        modelAndView.setViewName("profile");
        return modelAndView;
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView switchUserRole(@PathVariable("id") UUID id,@AuthenticationPrincipal AuthUser authUser) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(id);
        User loggedInUser = userService.getById(authUser.getUserId());
        userService.switchUserRole(user,loggedInUser);
        modelAndView.setViewName("redirect:/admin/users");
        return modelAndView;
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView switchUserStatus(@PathVariable("id") UUID id,@AuthenticationPrincipal AuthUser authUser) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(id);
        User loggedInUser = userService.getById(authUser.getUserId());
        userService.switchUserStatus(user,loggedInUser);
        modelAndView.setViewName("redirect:/admin/users");
        return modelAndView;
    }
}
