package app.web;

import app.notification.service.NotificationService;
import app.security.AuthUser;
import app.user.model.User;
import app.user.service.UserService;
import app.notification.dto.SendEmailRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/contact")
public class ContactController {
    private final UserService userService;
    private final NotificationService notificationService;

    public ContactController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ModelAndView getContactPage(@AuthenticationPrincipal AuthUser authUser) {
        User user = userService.getById(authUser.getUserId());
        ModelAndView mav = new ModelAndView("contact");
        mav.addObject("user", user);
        mav.addObject("contactRequest",
                SendEmailRequest.builder()
                        .senderUsername(user.getUsername())
                        .senderEmail(user.getEmail()).build());
        return mav;
    }

    @PostMapping
    public ModelAndView getMessageFromUser(@AuthenticationPrincipal AuthUser authUser,
                                           SendEmailRequest sendEmailRequest) {
        ModelAndView mav = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        notificationService.createNotificationFromUserToAdmin(user,sendEmailRequest);
        mav.setViewName("redirect:/contact");
        return mav;
    }
}
