package app.web;

import app.review.model.Review;
import app.review.service.ReviewService;
import app.security.AuthUser;
import app.web.dto.ReviewRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("")
    public ModelAndView addReview(@Valid  ReviewRequest reviewRequest, @AuthenticationPrincipal AuthUser authUser) {
        ModelAndView modelAndView = new ModelAndView();
        reviewService.addReview(reviewRequest);
        modelAndView.setViewName("redirect:/records/" + reviewRequest.getRecordId());
        return modelAndView;
    }
}
