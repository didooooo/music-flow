package app.web;

import app.security.AuthUser;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditAccountRequest;
import org.springframework.core.convert.ConversionService;
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
    private final ConversionService conversionService;

    public UserController(UserService userService, ConversionService conversionService) {
        this.userService = userService;
        this.conversionService = conversionService;
    }

    @GetMapping("/profile")
    public ModelAndView getProfilePage(@RequestParam("username") String username, @AuthenticationPrincipal AuthUser authUser) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        modelAndView.addObject("user", user);
        EditAccountRequest convert = conversionService.convert(user, EditAccountRequest.class);
        modelAndView.setViewName("profile");
        modelAndView.addObject("editAccountRequest", convert);
        return modelAndView;
    }

    @PutMapping("/profile/{id}")
    public ModelAndView editProfile(EditAccountRequest editAccountRequest, @PathVariable UUID id) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(id);
        User editedUser = userService.editInfo(user, editAccountRequest);
        modelAndView.setViewName("redirect:/users/profile?username="+editedUser.getUsername());
        return modelAndView;
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView switchUserRole(@PathVariable("id") UUID id, @AuthenticationPrincipal AuthUser authUser) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(id);
        User loggedInUser = userService.getById(authUser.getUserId());
        userService.switchUserRole(user, loggedInUser);
        modelAndView.setViewName("redirect:/admin/users");
        return modelAndView;
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView switchUserStatus(@PathVariable("id") UUID id, @AuthenticationPrincipal AuthUser authUser) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(id);
        User loggedInUser = userService.getById(authUser.getUserId());
        userService.switchUserStatus(user, loggedInUser);
        modelAndView.setViewName("redirect:/admin/users");
        return modelAndView;
    }
    @DeleteMapping("/profile/{id}")
    public ModelAndView deleteProfile(@PathVariable("id") UUID id) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(id);
        userService.deleteUser(user);
        modelAndView.setViewName("redirect:/logout");
        return modelAndView;
    }
}
