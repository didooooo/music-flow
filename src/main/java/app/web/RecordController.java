package app.web;

import app.artist.model.Artist;
import app.artist.service.ArtistService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.web.dto.RecordUpsertRequest;
import app.web.dto.SearchRecordByNameRequest;
import jakarta.validation.Valid;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/records")
public class RecordController {
    private final RecordService recordService;
    private final ConversionService conversionService;
    private final ArtistService artistService;

    public RecordController(RecordService recordService, ConversionService conversionService, ArtistService artistService) {
        this.recordService = recordService;
        this.conversionService = conversionService;
        this.artistService = artistService;
    }
    @GetMapping("/")
    public ModelAndView getAllRecords() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("products");

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
    public ModelAndView updateRecord(@PathVariable UUID id,@RequestParam(defaultValue = "0",name = "page") int page,@Valid  RecordUpsertRequest record) {
        ModelAndView mav = new ModelAndView();
        recordService.upsertRecord( record.toBuilder().id(id).build());
        Page<Record> records = recordService.getEightRecords(PageRequest.of(page, 8));
        List<Artist> artists = artistService.findAll();
        mav.setViewName("redirect:/admin/records");
        mav.addObject("records", records);
        mav.addObject("recordsRequest",  RecordUpsertRequest.builder().artists(artists).build());
        return mav;
    }
    @GetMapping("/search")
    public ModelAndView getSearchedRecordsByName(@RequestParam(value = "name",required = false) String name,@RequestParam(defaultValue = "0",name = "page") int page) {
        ModelAndView mav = new ModelAndView();
        Page<Record> records = recordService.getEightRecordsByName(PageRequest.of(page, 8),name);
        List<Artist> artists = artistService.findAll();
        mav.setViewName("admin-products");
        mav.addObject("records", records);
        mav.addObject("page", "records");
        mav.addObject("search", SearchRecordByNameRequest.builder().name(name).build());
        mav.addObject("recordsRequest",  RecordUpsertRequest.builder().artists(artists).build());
        return mav;
    }

}
