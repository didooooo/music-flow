package app.web;

import app.artist.service.ArtistService;
import app.notification.dto.SendEmailRequest;
import app.notification.service.NotificationService;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.review.service.ReviewService;
import app.security.CustomAuthenticationSuccessHandler;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ReviewRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static app.TestBuilder.aRandomAuthAdmin;
import static app.TestBuilder.aRandomAuthUser;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
public class ReviewControllerApiTest {
    @MockitoBean
    private  ReviewService reviewService;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void addReview_shouldAddReviewAndRedirect() throws Exception {
        // Arrange
        UUID recordId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setRecordId(recordId);
        reviewRequest.setUserId(userId);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great record!");

        MockHttpServletRequestBuilder request = post("/reviews")
                .with(user(aRandomAuthUser()))
                .with(csrf())
                .flashAttr("reviewRequest", reviewRequest);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/records/" + recordId));

        verify(reviewService).addReview(reviewRequest);
    }
}
