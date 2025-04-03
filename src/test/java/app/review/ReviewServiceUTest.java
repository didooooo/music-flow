package app.review;

import app.record.service.RecordService;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.review.service.ReviewService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ReviewRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.record.model.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceUTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserService userService;
    @Mock
    private RecordService recordService;
    @InjectMocks
    private ReviewService reviewService;

    @Test
    void givenRecordsWithReviews_whenGetRatingByGivenRecords_thenCalculateCorrectly() {
        // Given
        Review built = Review.builder().rating(4).description("test").build();
        Record record1 = Record.builder()
                .reviews(List.of(built))
                .build();
        Review built2 = Review.builder().rating(5).description("test").build();
        Record record2 = Record.builder()
                .reviews(List.of(built2))
                .build();
        Review built3 = Review.builder().rating(2).description("test").build();
        Record record3 = Record.builder()
                .reviews(List.of(built3))
                .build();


        List<Record> records = List.of(record1, record2, record3);

        // When
        List<Integer> ratings = reviewService.getRatingByGivenRecords(records);

        // Then
        assertEquals(List.of(4, 5, 2), ratings);
    }

    @Test
    void givenRecordsWithoutReviews_whenGetRatingByGivenRecords_thenCalculateCorrectly() {
        // Given
        Review built = Review.builder().rating(4).description("test").build();
        Review built2 = Review.builder().rating(5).description("test").build();
        Record record2 = Record.builder()
                .reviews(List.of(built2, built))
                .build();
        Record record3 = Record.builder()
                .reviews(new ArrayList<>())
                .build();


        List<Record> records = List.of(record2, record3);

        // When
        List<Integer> ratings = reviewService.getRatingByGivenRecords(records);

        // Then
        assertEquals(List.of(4, 0), ratings);
    }

    @Test
    void givenValidReviewRequest_whenAddReview_thenReviewIsSavedAndLinked() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        ReviewRequest reviewRequest = new ReviewRequest(userId, recordId, 5, "Great album!");
        User user = User.builder().id(userId).build();
        Record record = Record.builder().id(recordId).build();
        Review review = Review.builder()
                .user(user)
                .record(record)
                .rating(reviewRequest.getRating())
                .description(reviewRequest.getComment())
                .build();

        when(userService.getById(reviewRequest.getUserId())).thenReturn(user);
        when(recordService.findById(reviewRequest.getRecordId())).thenReturn(record);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        reviewService.addReview(reviewRequest);

        // Then
        verify(userService).addReviewToUserProfile(user, review);
        verify(recordService).addReviewToTheRecord(record, review);
        verify(reviewRepository).save(any(Review.class));
    }
}
