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
@RequestMapping("/shopping-cart")
public class ShoppingCartController {
    private final UserService userService;
    private final RecordService recordService;

    public ShoppingCartController(UserService userService, RecordService recordService) {
        this.userService = userService;
        this.recordService = recordService;
    }

    @PostMapping("/{id}")
    public ModelAndView addToCart(@PathVariable UUID id, @AuthenticationPrincipal AuthUser user) {
        ModelAndView mav = new ModelAndView();
        Record record = recordService.findById(id);
        userService.addRecordToCart(record,user.getUserId());
        mav.setViewName("redirect:/records/all?sort=");
        return mav;
    }
}
