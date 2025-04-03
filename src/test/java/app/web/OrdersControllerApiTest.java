package app.web;

import app.address.model.Address;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.security.CustomAuthenticationSuccessHandler;
import app.shopping_cart.model.ShoppingCart;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.OrderShippingRequest;
import app.web.dto.PaymentBankTransferRequest;
import app.web.dto.PaymentCreditCardRequest;
import app.web.dto.PaymentPayPalRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static app.TestBuilder.aRandomAuthUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdersController.class)
public class OrdersControllerApiTest {
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private UserService userService;
    @Autowired
    private ConversionService conversionService;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private MockMvc mockMvc;


    @Test
    void postRequestToMakeOrder_shouldRedirectToOrderPreview() throws Exception {
        OrderShippingRequest shippingRequest = new OrderShippingRequest();
        MockHttpServletRequestBuilder request = post("/orders/shipping")
                .with(user(aRandomAuthUser()))
                .with(csrf())
                .flashAttr("shippingRequest", shippingRequest);
        User user = new User();
        user.setId(UUID.randomUUID());
        UUID orderId = UUID.randomUUID();
        when(userService.getById(any(UUID.class))).thenReturn(user);
        when(orderService.createNewOrder(any(OrderShippingRequest.class), eq(user))).thenReturn(Order.builder().id(orderId).build());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/orders/" + orderId + "/preview"));
    }

    @Test
    void getRequestToPaymentPage_shouldReturnPaymentPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/orders/{id}/payment", UUID.randomUUID())
                .with(user(aRandomAuthUser()));
        User user = new User();
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        when(userService.getById(any(UUID.class))).thenReturn(user);
        when(orderService.getById(any(UUID.class))).thenReturn(order);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("order-payment"))
                .andExpect(model().attributeExists("user", "order", "bankTransfer", "paypal", "creditCard"));
    }

    @Test
    void postRequestToAddPayment_shouldRedirectToHome() throws Exception {
        PaymentBankTransferRequest bankTransferRequest = new PaymentBankTransferRequest();
        PaymentCreditCardRequest creditCardRequest = new PaymentCreditCardRequest();
        PaymentPayPalRequest payPalRequest = new PaymentPayPalRequest();

        MockHttpServletRequestBuilder request = post("/orders/{id}/payment", UUID.randomUUID())
                .flashAttr("paymentBankTransferRequest", bankTransferRequest)
                .flashAttr("paymentCreditCardRequest", creditCardRequest)
                .flashAttr("paymentPayPalRequest", payPalRequest)
                .with(csrf())
                .with(user(aRandomAuthUser()));

        when(orderService.makeOrder(any(UUID.class), any(), any(), any())).thenReturn(new Order());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));
    }

    @Test
    void getRequestToFinalPreview_shouldReturnPreviewPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/orders/{id}/preview", UUID.randomUUID())
                .with(user(aRandomAuthUser()));
        User user = new User();
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPrice(BigDecimal.TEN)
                .address(new Address(UUID.randomUUID(), "test", "test", "test", "test"))
                .build();
        user.setOrders(new ArrayList<>());
        user.setWishlist(new ArrayList<>());
        user.setReviews(new ArrayList<>());
        when(userService.getById(any(UUID.class))).thenReturn(user);
        when(orderService.getById(any(UUID.class))).thenReturn(order);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("order-preview"))
                .andExpect(model().attributeExists("user", "orders"));
    }

    @Test
    void getRequestToPendingOrders_shouldReturnPendingOrdersPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/orders/pending")
                .with(user(aRandomAuthUser()));

        User user = new User();
        List<Order> orders = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        user.setOrders(orders);
        user.setWishlist(new ArrayList<>());
        user.setReviews(new ArrayList<>());

        when(userService.getById(any(UUID.class))).thenReturn(user);
        when(orderService.getPendingOrdersByGivenUser(any(User.class))).thenReturn(orders);
        when(orderService.getTotalQuantityByGivenOrders(anyList())).thenReturn(quantities);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("pending-orders"))
                .andExpect(model().attributeExists("user", "orders", "totalQuantitiesForOrders"));
    }

    @Test
    void getRequestToOrderHistory_shouldReturnOrderHistoryPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/orders/history")
                .with(user(aRandomAuthUser()));

        User user = new User();
        List<Order> orders = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        user.setOrders(orders);
        user.setWishlist(new ArrayList<>());
        user.setReviews(new ArrayList<>());

        when(userService.getById(any(UUID.class))).thenReturn(user);
        when(orderService.getTotalQuantityByGivenOrders(anyList())).thenReturn(quantities);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("user-orders-history"))
                .andExpect(model().attributeExists("user", "orders", "totalQuantitiesForOrders"));
    }

    @Test
    void getRequestToOrderDetail_shouldReturnOrderDetailPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/orders/{id}", UUID.randomUUID())
                .with(user(aRandomAuthUser()));

        User user = new User();
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .address(new Address(UUID.randomUUID(), "test", "test", "test", "test"))
                .build();

        when(userService.getById(any(UUID.class))).thenReturn(user);
        when(orderService.getById(any(UUID.class))).thenReturn(order);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("order-status"))
                .andExpect(model().attributeExists("user", "order"));
    }


}
