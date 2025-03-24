package app.order.service;

import app.address.model.Address;
import app.address.service.AddressService;
import app.order.model.Order;
import app.order.model.OrderInfo;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.payment.model.Payment;
import app.payment.service.PaymentService;
import app.record.service.RecordService;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.model.ShoppingCartInfo;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.OrderShippingRequest;
import app.web.dto.PaymentBankTransferRequest;
import app.web.dto.PaymentCreditCardRequest;
import app.web.dto.PaymentPayPalRequest;
import jakarta.transaction.Transactional;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ConversionService conversionService;
    private final AddressService addressService;
    private final OrderInfoService orderInfoService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final StatisticService statisticService;
    private final RecordService recordService;

    public OrderService(OrderRepository orderRepository, ConversionService conversionService, AddressService addressService, OrderInfoService orderInfoService, PaymentService paymentService, UserService userService, StatisticService statisticService, RecordService recordService) {
        this.orderRepository = orderRepository;
        this.conversionService = conversionService;
        this.addressService = addressService;
        this.orderInfoService = orderInfoService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.statisticService = statisticService;
        this.recordService = recordService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getLastThreeOrders() {
        return orderRepository.findTop3ByOrderByCreatedAtDesc();
    }

    public Page<Order> getEightOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional
    public Order createNewOrder(OrderShippingRequest shippingRequest, User fromDB) {
        List<Order> allByStatus = orderRepository.findAllByStatus(OrderStatus.PENDING);
        ShoppingCart shoppingCart = fromDB.getShoppingCart();
        Address address = addressService.save(shippingRequest.getStreet(), shippingRequest.getCity(), shippingRequest.getState(), shippingRequest.getZip());
        for (Order order : allByStatus) {
            if (order.getOrderInfos().size() == shoppingCart.getShoppingCartInfos().size()) {
                int foundMatchingRecordsSize = getFoundMatchingRecordsSize(order, shoppingCart);
                if (foundMatchingRecordsSize == order.getOrderInfos().size()) {
                    boolean declinedQuantity= checkForQuantity(order.getOrderInfos());
                    order.setAddress(address);
                    order.setFirstName(shippingRequest.getFirstName());
                    order.setLastName(shippingRequest.getLastName());
                    order.setEmail(shippingRequest.getEmail());
                    order.setPhone(shippingRequest.getPhone());
                    if(declinedQuantity) {
                        order.setStatus(OrderStatus.CANCELLED);
                    }
                    return orderRepository.save(order);
                }
            }
        }
        Order order = conversionService.convert(shippingRequest, Order.class);
        order.setAddress(address);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(shoppingCart.getTotalPrice());
        order.setUser(fromDB);
        List<OrderInfo> orderInfos = new ArrayList<>();
        for (ShoppingCartInfo shoppingCartInfo : shoppingCart.getShoppingCartInfos()) {
            OrderInfo orderInfo = OrderInfo.builder()
                    .order(order)
                    .quantity(shoppingCartInfo.getQuantity())
                    .record(shoppingCartInfo.getRecord())
                    .build();
            OrderInfo saved = orderInfoService.save(orderInfo);
            orderInfos.add(saved);
        }
        order.setOrderInfos(orderInfos);
        boolean declinedQuantity= checkForQuantity(order.getOrderInfos());
        if(declinedQuantity) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        Order saved = orderRepository.save(order);
        userService.addOrderToUserOrders(saved, fromDB);
        Statistics statisticsForToday = statisticService.getStatisticsForToday();
        if(!declinedQuantity) {
            statisticsForToday.setPendingOrders(statisticsForToday.getPendingOrders() + 1);
        }
        statisticsForToday.setTotalOrders(statisticsForToday.getTotalOrders() + 1);
        statisticService.save(statisticsForToday);
        return saved;
    }

    private boolean checkForQuantity(List<OrderInfo> orderInfos) {
        for (OrderInfo orderInfo : orderInfos) {
            if(orderInfo.getRecord().getQuantity() < orderInfo.getQuantity()) {
                return true;
            }
        }
        return false;
    }

    private int getFoundMatchingRecordsSize(Order order, ShoppingCart shoppingCart) {
        int foundOrderSize = 0;
        for (int i = 0; i < order.getOrderInfos().size(); i++) {
            boolean foundOrder = false;
            OrderInfo orderInfo = order.getOrderInfos().get(i);//espresso
            for (int j = 0; j < shoppingCart.getShoppingCartInfos().size(); j++) {
                ShoppingCartInfo shoppingCartInfo = shoppingCart.getShoppingCartInfos().get(j);
                if ((orderInfo.getRecord().getId().equals(shoppingCartInfo.getRecord().getId()) && orderInfo.getQuantity() == shoppingCartInfo.getQuantity())) {
                    foundOrder = true;
                    break;
                }
            }
            if (foundOrder) {
                foundOrderSize++;
            }
        }
        return foundOrderSize;
    }

    public Order getById(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public Order makeOrder(UUID id, PaymentBankTransferRequest paymentBankTransferRequest, PaymentCreditCardRequest paymentCreditCardRequest, PaymentPayPalRequest payPalRequest) {
        Order order = getById(id);
        Payment payment = paymentService.createPayment(order, paymentBankTransferRequest, paymentCreditCardRequest, payPalRequest);
        order.setPayment(payment);
        order.setStatus(OrderStatus.CONFIRMED);
        userService.clearShoppingCart(order.getUser());
        Order saved = orderRepository.save(order);
        recordService.updateRecordsQuantityAfterOrder(order.getOrderInfos());
        Statistics statisticsForToday = statisticService.getStatisticsForToday();
        statisticsForToday.setPendingOrders(statisticsForToday.getPendingOrders() - 1);
        statisticsForToday.setTotalMoney(statisticsForToday.getTotalMoney().add(order.getTotalPrice()));
        statisticService.save(statisticsForToday);
        return saved;
    }

    public List<Order> getPendingOrdersByGivenUser(User user) {
        List<Order> orders = user.getOrders();
        List<Order> pendingOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus().equals(OrderStatus.PENDING)) {
                pendingOrders.add(order);
            }
        }
        return pendingOrders;
    }

    public Order updateDataOfExistingOrder(UUID orderId, OrderShippingRequest shippingRequest) {
        if (shippingRequest.getOrderId() == null) {
            return null;
        }
        Optional<Order> orderFromDB = orderRepository.findById(orderId);
        if (orderFromDB.isPresent()) {
            Order order = orderFromDB.get();
            Address address = addressService.save(shippingRequest.getStreet(), shippingRequest.getCity(), shippingRequest.getState(), shippingRequest.getZip());
            order.setAddress(address);
            order.setFirstName(shippingRequest.getFirstName());
            order.setLastName(shippingRequest.getLastName());
            order.setEmail(shippingRequest.getEmail());
            order.setPhone(shippingRequest.getPhone());
            return orderRepository.save(order);
        }
        return null;
    }

    public List<Integer> getTotalQuantityByGivenOrders(List<Order> orders) {
        List<Integer> totalQuantitiesForOrders = new ArrayList<>();
        for (Order order : orders) {
            int quantity = 0;
            for (OrderInfo orderInfo : order.getOrderInfos()) {
                quantity += orderInfo.getQuantity();
            }
            totalQuantitiesForOrders.add(quantity);
        }
        return totalQuantitiesForOrders;
    }
}
