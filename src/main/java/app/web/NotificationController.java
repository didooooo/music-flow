package app.web;

import app.notification.dto.Notification;
import app.notification.service.NotificationService;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ModelAndView getAllNotifications() {
        ModelAndView mav = new ModelAndView();
        List<Notification> notifications = notificationService.getAll();
        List<String> prettyDateFormat = getDateFormatted(notifications);
        mav.setViewName("admin-notification");
        mav.addObject("notifications", notifications);
        mav.addObject("page", "notifications");
        mav.addObject("criteria", "all");
        mav.addObject("prettyDateFormat", prettyDateFormat);
        return mav;
    }

    @GetMapping("/read")
    public ModelAndView getAllReadNotifications() {
        ModelAndView mav = new ModelAndView();
        List<Notification> notifications = notificationService.getAllRead(true);
        List<String> prettyDateFormat = getDateFormatted(notifications);
        mav.setViewName("admin-notification");
        mav.addObject("notifications", notifications);
        mav.addObject("page", "notifications");
        mav.addObject("criteria", "read");
        mav.addObject("prettyDateFormat", prettyDateFormat);
        return mav;
    }

    @GetMapping("/unread")
    public ModelAndView getAllUnreadNotifications() {
        ModelAndView mav = new ModelAndView();
        List<Notification> notifications = notificationService.getAllRead(false);
        List<String> prettyDateFormat = getDateFormatted(notifications);
        mav.setViewName("admin-notification");
        mav.addObject("notifications", notifications);
        mav.addObject("page", "notifications");
        mav.addObject("criteria", "unread");
        mav.addObject("prettyDateFormat", prettyDateFormat);
        return mav;
    }
    @DeleteMapping("/{id}")
    public ModelAndView deleteNotification(@PathVariable UUID id) {
        ModelAndView mav = new ModelAndView();
        notificationService.deleteNotificationById(id);
        mav.setViewName("redirect:/notifications");
        return mav;
    }
    @PutMapping("/{id}")
    public ModelAndView updateNotification(@PathVariable UUID id) {
        ModelAndView mav = new ModelAndView();
        notificationService.updateNotification(id);
        mav.setViewName("redirect:/notifications");
        return mav;
    }

    private List<String> getDateFormatted(List<Notification> notifications) {
        List<String> prettyDateFormat = new ArrayList<>();
        PrettyTime pt = new PrettyTime();
        notifications.forEach(notification -> {
            prettyDateFormat.add(pt.format(notification.getCreatedAt()));
        });
        return prettyDateFormat;
    }
}
