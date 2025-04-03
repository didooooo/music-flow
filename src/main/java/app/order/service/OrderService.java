package app.order.service;

import app.address.model.Address;
import app.address.service.AddressService;
import app.exception.OrderDoesNotExistException;
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
import app.utils.DateUtils;
import app.web.dto.*;
import jakarta.persistence.criteria.Join;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

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

    public OrderService(OrderRepository orderRepository, ConversionService conversionService, AddressService addressService, OrderInfoService orderInfoService, PaymentService paymentService, UserService userService, StatisticService statisticService, @Lazy RecordService recordService) {
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
                    boolean declinedQuantity = checkForQuantity(order.getOrderInfos());
                    order.setAddress(address);
                    order.setFirstName(shippingRequest.getFirstName());
                    order.setLastName(shippingRequest.getLastName());
                    order.setEmail(shippingRequest.getEmail());
                    order.setPhone(shippingRequest.getPhone());
                    if (declinedQuantity) {
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
        boolean declinedQuantity = checkForQuantity(order.getOrderInfos());
        if (declinedQuantity) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        Order saved = orderRepository.save(order);
        userService.addOrderToUserOrders(saved, fromDB);
        Statistics statisticsForToday = statisticService.getStatisticsForToday();
        if (!declinedQuantity) {
            statisticsForToday.setPendingOrders(statisticsForToday.getPendingOrders() + 1);
        }
        statisticsForToday.setTotalOrders(statisticsForToday.getTotalOrders() + 1);
        statisticService.save(statisticsForToday);
        return saved;
    }

    private boolean checkForQuantity(List<OrderInfo> orderInfos) {
        for (OrderInfo orderInfo : orderInfos) {
            if (orderInfo.getRecord().getQuantity() < orderInfo.getQuantity()) {
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
        return orderRepository.findById(id).orElseThrow(() -> new OrderDoesNotExistException("Order not found"));
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

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Page<Order> getEightOrders(PageRequest of, OrderFilterRequest orderFilterRequest) {
        Specification<Order> spec = Specification.where(null);
        spec = criteriaBuilder(spec, orderFilterRequest);
        return orderRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize()));
    }

    private Specification<Order> criteriaBuilder(Specification<Order> spec, OrderFilterRequest orderFilterRequest) {
        if (!orderFilterRequest.getProductsName().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> {
                Join<Order, OrderInfo> orderInfoJoin = root.join("orderInfos");
                return criteriaBuilder.like(criteriaBuilder.lower(orderInfoJoin.get("record").get("title")), "%" + orderFilterRequest.getProductsName().toLowerCase() + "%");
            });
        }

        if (orderFilterRequest.getOrderStatus() != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("status")).value(orderFilterRequest.getOrderStatus()));
        }

        if (orderFilterRequest.getFromDate() != null && orderFilterRequest.getToDate() != null) {
            LocalDateTime fromDateTime = orderFilterRequest.getFromDate().atStartOfDay();
            LocalDateTime toDateTime = orderFilterRequest.getToDate().atTime(23, 59, 59);
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get("createdAt"), fromDateTime, toDateTime)
            );
        }
        if (orderFilterRequest.getFromDate() != null && orderFilterRequest.getToDate() == null) {
            LocalDateTime fromDateTime = orderFilterRequest.getFromDate().atStartOfDay();
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime)
            );
        }
        if (orderFilterRequest.getFromDate() == null && orderFilterRequest.getToDate() != null) {
            LocalDateTime toDateTime = orderFilterRequest.getToDate().atTime(23, 59, 59);
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDateTime)
            );
        }
        return spec;
    }

    public List<Order> getOrdersByDay(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findAllByCreatedAtBetween(from, to);
    }
    public Map<String, Integer> getChartInfo(String period) {
        if (period.equals("week")) {
            return getChartInfoForOneWeek();
        } else if (period.equals("month")) {
            YearMonth month = YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth());
            List<DateUtils> weeks = getWeeksByISO(month);
            return getStringIntegerMap(weeks);
        } else if (period.equals("year")) {
            List<DateUtils> dateUtils = getForMonth();
            return getStringIntegerMap(dateUtils);
        }
        return null;
    }

    private Map<String, Integer> getStringIntegerMap(List<DateUtils> dateUtils) {
        Map<String, Integer> chartInfo = new LinkedHashMap<>();
        for (DateUtils month : dateUtils) {
            List<Order> orders = getOrdersByDay(month.getStartDateTime(), month.getEndDateTime());
            chartInfo.put(month.getLabel(), orders.size());
        }
        return chartInfo;
    }

    private List<DateUtils> getForMonth() {
        List<DateUtils> dateUtils = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate firstDay = LocalDate.of(LocalDate.now().getYear(), month, 1);
            LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
            dateUtils.add(new DateUtils(
                    firstDay.getMonth().name(),
                    firstDay.atStartOfDay(),
                    lastDay.atTime(23, 59, 59)
            ));
        }
        return dateUtils;
    }

    private List<DateUtils> getWeeksByISO(YearMonth month) {
        List<DateUtils> dateUtils = new ArrayList<>();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d").localizedBy(Locale.ENGLISH);
        while (!start.isAfter(end)) {
            LocalDate weekEnd = start.with(DayOfWeek.SUNDAY);
            if (weekEnd.isAfter(end)) weekEnd = end;
            String label;
            if (start.getDayOfMonth() == 1) {
                label = "Begin: - " + weekEnd.format(formatter);
            } else if (weekEnd.equals(end)) {
                label = start.format(formatter) + " - End";
            } else {
                label = start.format(formatter) + " - " + weekEnd.format(formatter);
            }
            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);
            dateUtils.add(new DateUtils(label, startDateTime, endDateTime));
            start = weekEnd.plusDays(1);
        }

        return dateUtils;
    }


    private Map<String, Integer> getChartInfoForOneWeek() {
        Map<String, Integer> chartInfo = new LinkedHashMap<>();
        int days = 0;
        while (days < 7) {
            LocalDate today = LocalDate.now();
            today = today.minusDays(days);
            List<Order> orders = getOrdersByDay(today.atStartOfDay(), today.atTime(23, 59, 59));
            chartInfo.put(today.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), orders.size());
            days++;
        }
        return chartInfo;
    }
}
