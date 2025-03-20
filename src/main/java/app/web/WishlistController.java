package app.web;

import app.record.model.Record;
import app.record.service.RecordService;
import app.review.service.ReviewService;
import app.security.AuthUser;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    private final RecordService recordService;
    private final UserService userService;
    private final ReviewService reviewService;

    public WishlistController(RecordService recordService, UserService userService, ReviewService reviewService) {
        this.recordService = recordService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @PostMapping("/{id}")
    public ModelAndView addToWishList(@PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser) {
        ModelAndView mav = new ModelAndView();
        Record record = recordService.findById(id);
        userService.addRecordToWishlist(authUser.getUserId(),record);
        mav.setViewName("redirect:/records/all?sort=");
        return mav;
    }
    @GetMapping
    public ModelAndView getWishlist(@AuthenticationPrincipal AuthUser authUser) {
        ModelAndView mav = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        List<Integer> ratings = reviewService.getRatingByGivenRecords(user.getWishlist());
        mav.setViewName("wishlist");
        mav.addObject("records", user.getWishlist());
        mav.addObject("user", user);
        mav.addObject("ratings", ratings);
        return mav;
    }
    @DeleteMapping("/{id}")
    public ModelAndView deleteRecordFromWishlist(@PathVariable UUID id,@AuthenticationPrincipal AuthUser authUser) {
        ModelAndView mav = new ModelAndView();
        Record record = recordService.findById(id);
        User user = userService.getById(authUser.getUserId());
        userService.removeFromWishlist(user,record);
        mav.setViewName("redirect:/wishlist");
        return mav;
    }
}
