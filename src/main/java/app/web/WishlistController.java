package app.web;

import app.record.model.Record;
import app.record.service.RecordService;
import app.security.AuthUser;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    private final RecordService recordService;
    private final UserService userService;

    public WishlistController(RecordService recordService, UserService userService) {
        this.recordService = recordService;
        this.userService = userService;
    }

    @PostMapping("/{id}")
    public ModelAndView addToWishList(@PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser) {
        ModelAndView mav = new ModelAndView();
        Record record = recordService.findById(id);
        userService.addRecordToWishlist(authUser.getUserId(),record);
        mav.setViewName("redirect:/records/all?sort=");
        return mav;
    }
}
