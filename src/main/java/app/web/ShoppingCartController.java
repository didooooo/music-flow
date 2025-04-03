package app.web;

import app.record.model.Record;
import app.record.service.RecordService;
import app.security.AuthUser;
import app.shopping_cart.model.ShoppingCartInfo;
import app.shopping_cart.service.ShoppingCartInfoService;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/shopping-cart")
public class ShoppingCartController {
    private final UserService userService;
    private final RecordService recordService;
    private final ShoppingCartInfoService shoppingCartInfoService;

    public ShoppingCartController(UserService userService, RecordService recordService, ShoppingCartInfoService shoppingCartInfoService) {
        this.userService = userService;
        this.recordService = recordService;
        this.shoppingCartInfoService = shoppingCartInfoService;
    }

    @PostMapping("/{id}")
    public ModelAndView addToCart(@PathVariable UUID id,
                                  @AuthenticationPrincipal AuthUser user,
                                  @RequestHeader(value = "Referer", required = false) String referer) {
        ModelAndView mav = new ModelAndView();
        Record record = recordService.findById(id);
        userService.addRecordToCart(record, user.getUserId());
        mav.setViewName("redirect:" + (referer != null ? referer : "/records/all?sort="));
        return mav;
    }

    @GetMapping()
    public ModelAndView showCart(@AuthenticationPrincipal AuthUser user) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("shopping-cart");
        User fromDB = userService.getById(user.getUserId());
        List<ShoppingCartInfo> records = userService.getShoppingCartRecords(user.getUserId());
        mav.addObject("user", fromDB);
        mav.addObject("records", records);
        return mav;
    }

    @PutMapping("/minus/{id}")
    public ModelAndView decreaseQuantity(@PathVariable UUID id, @AuthenticationPrincipal AuthUser user) {
        Record record = recordService.findById(id);
        User fromDB = userService.getById(user.getUserId());
        shoppingCartInfoService.decreaseQuantityOfProduct(record,fromDB.getShoppingCart());
        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:/shopping-cart");
        return mav;
    }

    @PutMapping("/plus/{id}")
    public ModelAndView increaseQuantity(@PathVariable UUID id, @AuthenticationPrincipal AuthUser user) {
        Record record = recordService.findById(id);
        User fromDB = userService.getById(user.getUserId());
        shoppingCartInfoService.increaseQuantityOfProduct(record,fromDB.getShoppingCart());
        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:/shopping-cart");
        return mav;
    }
    @DeleteMapping("/{id}")
    public ModelAndView deleteRecord(@PathVariable UUID id, @AuthenticationPrincipal AuthUser user) {
        Record record = recordService.findById(id);
        User fromDB = userService.getById(user.getUserId());
        shoppingCartInfoService.deleteProduct(record,fromDB.getShoppingCart());
        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:/shopping-cart");
        return mav;
    }
}
