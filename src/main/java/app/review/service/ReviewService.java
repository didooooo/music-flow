package app.review.service;

import app.record.model.Record;
import app.review.model.Review;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {
    public List<Integer> getRatingByGivenRecords(List<Record> records) {
        List<Integer> ratings = new ArrayList<>();
        for (Record record : records) {
            int rating=0;
            for (Review review : record.getReviews()) {
                rating+=review.getRating();
            }
            ratings.add(rating);
        }
        return ratings;
    }
}
