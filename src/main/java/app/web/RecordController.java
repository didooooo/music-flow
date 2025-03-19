package app.web;

import app.artist.model.Artist;
import app.artist.service.ArtistService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.review.service.ReviewService;
import app.security.AuthUser;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RecordUpsertRequest;
import app.web.dto.SearchRecordByNameRequest;
import jakarta.validation.Valid;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/records")
public class RecordController {
    private final RecordService recordService;
    private final ConversionService conversionService;
    private final ArtistService artistService;
    private final ReviewService reviewService;
    private final UserService userService;

    public RecordController(RecordService recordService, ConversionService conversionService, ArtistService artistService, ReviewService reviewService, UserService userService) {
        this.recordService = recordService;
        this.conversionService = conversionService;
        this.artistService = artistService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping("/all")
    public ModelAndView getAllRecords(@RequestParam(defaultValue = "0", name = "page") int page,
                                      @RequestParam(name = "sort", required = false) String sort,
                                      @AuthenticationPrincipal AuthUser user) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("products");
        Page<Record> eightRecords = recordService.getRecordsWithGivenSize(PageRequest.of(page, 9),sort);
        List<Integer> ratingByGivenRecords = reviewService.getRatingByGivenRecords(eightRecords.getContent());
        User fromDB = userService.getById(user.getUserId());
        modelAndView.addObject("resultName","ALl Albums");
        modelAndView.addObject("sort",sort);
        modelAndView.addObject("records", eightRecords);
        modelAndView.addObject("user", fromDB);
        modelAndView.addObject("selectedFormats", new ArrayList<>());
        modelAndView.addObject("selectedGenres", new ArrayList<>());
        modelAndView.addObject("selectedTypes", new ArrayList<>());
        modelAndView.addObject("minPrice", null);
        modelAndView.addObject("maxPrice",null);
        modelAndView.addObject("ratings", ratingByGivenRecords);
        return modelAndView;
    }

    @GetMapping("/filter")
    public ModelAndView getAll(@RequestParam(defaultValue = "0", name = "page") int page,
                               @AuthenticationPrincipal AuthUser user,
                               @RequestParam(name = "sort", required = false) String sort,
                               @RequestParam(required = false, name = "format") List<String> format,
                               @RequestParam(required = false, name = "genre") List<String> genre,
                               @RequestParam(required = false, name = "media") List<String> media,
                               @RequestParam(required = false, name = "minPrice") BigDecimal minPrice,
                               @RequestParam(required = false, name = "maxPrice") BigDecimal maxPrice) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("products");
        if(format == null){
            format = new ArrayList<>();
        }
        if(genre == null){
            genre = new ArrayList<>();
        }
        if(media == null){
            media = new ArrayList<>();
        }
        if(media.isEmpty() && format.isEmpty() && genre.isEmpty()  && minPrice==null && maxPrice==null){
            modelAndView.setViewName("redirect:/records/all?sort="+sort);
            return modelAndView;
        }
        Page<Record> eightRecords = recordService.getEightRecordsWithFilterParameters(PageRequest.of(page, 1),sort,format,genre,media,maxPrice,minPrice);
        List<Integer> ratingByGivenRecords = reviewService.getRatingByGivenRecords(eightRecords.getContent());
        User fromDB = userService.getById(user.getUserId());
        modelAndView.addObject("records", eightRecords);
        modelAndView.addObject("resultName","Result from Filter");
        modelAndView.addObject("sort",sort);
        modelAndView.addObject("user", fromDB);
        modelAndView.addObject("selectedFormats", format);
        modelAndView.addObject("selectedGenres", genre);
        modelAndView.addObject("selectedTypes", media);
        modelAndView.addObject("minPrice", minPrice);
        modelAndView.addObject("maxPrice", maxPrice);
        modelAndView.addObject("ratings", ratingByGivenRecords);
        return modelAndView;
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView saveRecord(RecordUpsertRequest record) {
        ModelAndView mav = new ModelAndView();
        recordService.upsertRecord(record);
        mav.setViewName("redirect:/admin/records");
        return mav;
    }

    @PutMapping("/save/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView updateRecord(@PathVariable UUID id, @RequestParam(defaultValue = "0", name = "page") int page, @Valid RecordUpsertRequest record) {
        ModelAndView mav = new ModelAndView();
        recordService.upsertRecord(record.toBuilder().id(id).build());
        Page<Record> records = recordService.getRecordsWithGivenSize(PageRequest.of(page, 9),null);
        List<Artist> artists = artistService.findAll();
        mav.setViewName("redirect:/admin/records");
        mav.addObject("records", records);
        mav.addObject("recordsRequest", RecordUpsertRequest.builder().artists(artists).build());
        return mav;
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getSearchedRecordsByName(@RequestParam(value = "name", required = false) String name, @RequestParam(defaultValue = "0", name = "page") int page, @AuthenticationPrincipal AuthUser user) {
        ModelAndView mav = new ModelAndView();
        Page<Record> records = recordService.getEightRecordsByName(PageRequest.of(page, 8), name);
        List<Artist> artists = artistService.findAll();
        User fromDB = userService.getById(user.getUserId());
        mav.setViewName("admin-products");
        mav.addObject("records", records);
        mav.addObject("user", fromDB);
        mav.addObject("page", "records");
        mav.addObject("search", SearchRecordByNameRequest.builder().name(name).build());
        mav.addObject("recordsRequest", RecordUpsertRequest.builder().artists(artists).build());
        return mav;
    }
    @GetMapping("/{id}")
    public ModelAndView getRecordInfo(@PathVariable UUID id, @AuthenticationPrincipal AuthUser user) {
        ModelAndView mav = new ModelAndView();
        User userDB = userService.getById(user.getUserId());
        Record record = recordService.findById(id);
        mav.addObject("record", record);
        mav.addObject("user", userDB);
        mav.setViewName("product-info");
        return mav;
    }

}
