package app.review.service;

import app.record.model.Record;
import app.record.service.RecordService;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ReviewRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final RecordService recordService;


    public ReviewService(ReviewRepository reviewRepository, UserService userService, RecordService recordService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.recordService = recordService;
    }

    public List<Integer> getRatingByGivenRecords(List<Record> records) {
        List<Integer> ratings = new ArrayList<>();
        for (Record record : records) {
            int rating=0;
            for (Review review : record.getReviews()) {
                rating+=review.getRating();
            }
            if(!record.getReviews().isEmpty()) {
                ratings.add(rating / record.getReviews().size());
            }else{
                ratings.add(rating);
            }
        }
        return ratings;
    }
    @Transactional
    public void addReview(ReviewRequest reviewRequest) {
        User user = userService.getById(reviewRequest.getUserId());
        Record record = recordService.findById(reviewRequest.getRecordId());
        Review review = Review.builder()
                .description(reviewRequest.getComment())
                .user(user)
                .record(record)
                .rating(reviewRequest.getRating())
                .build();
        Review saved = reviewRepository.save(review);
        recordService.addReviewToTheRecord(record,saved);
        userService.addReviewToUserProfile(user,saved);
    }
}
