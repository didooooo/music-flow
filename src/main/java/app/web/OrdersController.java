package app.web;

import app.order.model.Order;
import app.order.service.OrderService;
import app.security.AuthUser;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.OrderShippingRequest;
import app.web.dto.PaymentCreditCardRequest;
import app.web.dto.PaymentPayPalRequest;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import app.web.dto.PaymentBankTransferRequest;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrdersController {
    private final OrderService orderService;
    private final ConversionService conversionService;
    private final UserService userService;

    public OrdersController(OrderService orderService, ConversionService conversionService, UserService userService) {
        this.orderService = orderService;
        this.conversionService = conversionService;
        this.userService = userService;
    }

    @GetMapping("/shipping")
    public ModelAndView getShippingPage(@AuthenticationPrincipal AuthUser authUser) {
        ModelAndView mav = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        mav.setViewName("order-shipping");
        OrderShippingRequest shippingRequest = conversionService.convert(user, OrderShippingRequest.class);
        mav.addObject("user", user);
        mav.addObject("records", user.getShoppingCart().getShoppingCartInfos());
        mav.addObject("shoppingCart", user.getShoppingCart());
        mav.addObject("shippingRequest", shippingRequest);
        return mav;
    }

    @GetMapping("/{id}/shipping")
    public ModelAndView getShippingPageForGivenOrder(@AuthenticationPrincipal AuthUser authUser, @PathVariable UUID id) {
        ModelAndView mav = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        Order order = orderService.getById(id);
        OrderShippingRequest shippingRequest = conversionService.convert(order, OrderShippingRequest.class);
        mav.setViewName("order-shipping");
        mav.addObject("user", user);
        mav.addObject("records", order.getOrderInfos());
        mav.addObject("shoppingCart", order);
        mav.addObject("shippingRequest", shippingRequest);
        return mav;
    }

    @PostMapping("/shipping")
    public ModelAndView makeOrder(@AuthenticationPrincipal AuthUser authUser, OrderShippingRequest shippingRequest) {
        ModelAndView mav = new ModelAndView();
        User fromDB = userService.getById(authUser.getUserId());
        Order order = orderService.updateDataOfExistingOrder(shippingRequest.getOrderId(),shippingRequest);
        if (order == null) {
            order = orderService.createNewOrder(shippingRequest, fromDB);
        }
        mav.setViewName(String.format("redirect:/orders/%s/preview", order.getId()));
        return mav;
    }

    @GetMapping("/{id}/payment")
    public ModelAndView getPaymentPage(@AuthenticationPrincipal AuthUser authUser, @PathVariable UUID id) {
        ModelAndView mav = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        Order order = orderService.getById(id);
        mav.setViewName("order-payment");
        mav.addObject("user", user);
        mav.addObject("order", order);
        mav.addObject("bankTransfer", new PaymentBankTransferRequest());
        mav.addObject("paypal", new PaymentPayPalRequest());
        mav.addObject("creditCard", new PaymentCreditCardRequest());
        return mav;
    }

    @PostMapping("/{id}/payment")
    public ModelAndView addPaymentToOrder(@AuthenticationPrincipal AuthUser authUser,
                                          @PathVariable UUID id,
                                          PaymentBankTransferRequest paymentBankTransferRequest,
                                          PaymentCreditCardRequest paymentCreditCardRequest,
                                          PaymentPayPalRequest payPalRequest) {
        ModelAndView mav = new ModelAndView();
//        User user = userService.getById(authUser.getUserId());
        orderService.makeOrder(id, paymentBankTransferRequest, paymentCreditCardRequest, payPalRequest);
        mav.setViewName("redirect:/home");
        return mav;
    }

    @GetMapping("/{id}/preview")
    public ModelAndView getFinalPreview(@AuthenticationPrincipal AuthUser authUser, @PathVariable UUID id) {
        ModelAndView mav = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        Order order = orderService.getById(id);
        mav.setViewName("order-preview");
        mav.addObject("user", user);
        mav.addObject("orders", order);
        return mav;
    }

    @GetMapping("/pending")
    public ModelAndView getPendingOrders(@AuthenticationPrincipal AuthUser authUser) {
        ModelAndView mav = new ModelAndView();
        User user = userService.getById(authUser.getUserId());
        List<Order> orderList = orderService.getPendingOrdersByGivenUser(user);
        mav.setViewName("pending-orders");
        mav.addObject("user", user);
        mav.addObject("orders", orderList);
        return mav;
    }
}
