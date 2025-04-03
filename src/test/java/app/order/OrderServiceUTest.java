package app.order;

import app.address.model.Address;
import app.address.service.AddressService;
import app.exception.OrderDoesNotExistException;
import app.order.model.Order;
import app.order.model.OrderInfo;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.order.service.OrderInfoService;
import app.order.service.OrderService;
import app.payment.model.Payment;
import app.payment.service.PaymentService;
import app.record.model.Record;
import app.record.service.RecordService;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.model.ShoppingCartInfo;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.User;
import app.user.service.UserService;
import app.utils.DateUtils;
import app.web.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ConversionService conversionService;
    @Mock
    private AddressService addressService;
    @Mock
    private OrderInfoService orderInfoService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private UserService userService;
    @Mock
    private StatisticService statisticService;
    @Mock
    private RecordService recordService;
    @InjectMocks
    private OrderService orderService;

    @Test
    void givenOrdersExist_whenGetAllOrders_thenReturnListOfOrders() {
        // Given
        List<Order> orders = List.of(new Order(), new Order());
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void givenOrdersExist_whenGetLastThreeOrders_thenReturnLastThreeOrders() {
        // Given
        List<Order> orders = List.of(new Order(), new Order(), new Order());
        when(orderRepository.findTop3ByOrderByCreatedAtDesc()).thenReturn(orders);

        // When
        List<Order> result = orderService.getLastThreeOrders();

        // Then
        assertEquals(3, result.size());
        verify(orderRepository, times(1)).findTop3ByOrderByCreatedAtDesc();
    }

    @Test
    void givenOrdersExist_whenGetEightOrders_thenReturnPageOfOrders() {
        // Given
        List<Order> orders = List.of(new Order(), new Order(), new Order());
        Page<Order> ordersPage = new PageImpl<>(orders);
        Pageable pageable = PageRequest.of(0, 8);
        when(orderRepository.findAll(pageable)).thenReturn(ordersPage);

        // When
        Page<Order> result = orderService.getEightOrders(pageable);

        // Then
        assertNotNull(result);
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void givenMatchingShoppingCart_whenCreateNewOrder_thenOrderUpdated() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setTotalPrice(BigDecimal.valueOf(100));
        shoppingCart.setTotalQuantity(2);
        user.setShoppingCart(shoppingCart);

        Order existingOrder = new Order();
        existingOrder.setStatus(OrderStatus.PENDING);
        existingOrder.setTotalPrice(BigDecimal.valueOf(100));
        Record record = Record.builder().id(UUID.randomUUID()).quantity(2).build();
        existingOrder.setOrderInfos(new ArrayList<>(List.of(OrderInfo.builder().record(record).quantity(2).build())));
        ShoppingCartInfo cartInfo = new ShoppingCartInfo();
        cartInfo.setQuantity(2);
        cartInfo.setRecord(record);
        shoppingCart.setShoppingCartInfos(List.of(cartInfo));
        Statistics statistics = Statistics.builder()
                .pendingOrders(1)
                .shippedOrders(0)
                .totalOrders(1)
                .build();
        OrderInfo orderInfo = OrderInfo.builder().record(record).quantity(2).build();
        Address address = new Address(UUID.randomUUID(), "test", "test", "te", "t");
        when(orderRepository.findAllByStatus(OrderStatus.PENDING)).thenReturn(Collections.singletonList(existingOrder));
        when(addressService.save(any(), any(), any(), any())).thenReturn(address);
        when(orderRepository.save(any())).thenReturn(Order.builder().status(OrderStatus.PENDING).user(user).build());

        // When
        Order createdOrder = orderService.createNewOrder(new OrderShippingRequest(), user);

        // Then
        assertNotNull(createdOrder);
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        assertEquals(user, createdOrder.getUser());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void givenNoMatchingShoppingCart_whenCreateNewOrder_thenCreateNewOrder() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setTotalPrice(BigDecimal.valueOf(150));
        shoppingCart.setTotalQuantity(3);
        ShoppingCartInfo cartInfo = new ShoppingCartInfo();
        shoppingCart.setShoppingCartInfos(List.of(cartInfo));
        user.setShoppingCart(shoppingCart);
        Record record = Record.builder().quantity(2).build();
        Address address = new Address(UUID.randomUUID(), "test", "test", "te", "t");
        OrderInfo orderInfo = OrderInfo.builder().record(record).build();
        Statistics statistics = Statistics.builder()
                .pendingOrders(1)
                .shippedOrders(0)
                .totalOrders(1)
                .build();
        when(orderRepository.findAllByStatus(OrderStatus.PENDING)).thenReturn(Collections.emptyList());
        when(addressService.save(any(), any(), any(), any())).thenReturn(address);
        when(orderInfoService.save(any(OrderInfo.class))).thenReturn(orderInfo);
        when(conversionService.convert(any(), eq(Order.class))).thenReturn(new Order());
        when(statisticService.getStatisticsForToday()).thenReturn(statistics);
        when(orderRepository.save(any())).thenReturn(Order.builder().status(OrderStatus.PENDING).user(user).build());

        // When
        Order returned = orderService.createNewOrder(new OrderShippingRequest(), user);

        // Then
        assertNotNull(returned);
        assertEquals(OrderStatus.PENDING, returned.getStatus());
        assertEquals(user, returned.getUser());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void givenNoMatchingShoppingCartAndNotEnoughQuantity_whenCreateNewOrder_thenCreateNewOrder() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setTotalPrice(BigDecimal.valueOf(150));
        shoppingCart.setTotalQuantity(3);
        ShoppingCartInfo cartInfo = new ShoppingCartInfo();
        shoppingCart.setShoppingCartInfos(List.of(cartInfo));
        user.setShoppingCart(shoppingCart);
        Record record = Record.builder().quantity(2).build();
        Address address = new Address(UUID.randomUUID(), "test", "test", "te", "t");
        OrderInfo orderInfo = OrderInfo.builder().record(record).quantity(3).build();
        Statistics statistics = Statistics.builder()
                .pendingOrders(1)
                .shippedOrders(0)
                .totalOrders(1)
                .build();
        when(orderRepository.findAllByStatus(OrderStatus.PENDING)).thenReturn(Collections.emptyList());
        when(addressService.save(any(), any(), any(), any())).thenReturn(address);
        when(orderInfoService.save(any(OrderInfo.class))).thenReturn(orderInfo);
        when(conversionService.convert(any(), eq(Order.class))).thenReturn(new Order());
        when(statisticService.getStatisticsForToday()).thenReturn(statistics);
        when(orderRepository.save(any())).thenReturn(Order.builder().status(OrderStatus.CANCELLED).user(user).build());

        // When
        Order returned = orderService.createNewOrder(new OrderShippingRequest(), user);

        // Then
        assertNotNull(returned);
        assertEquals(OrderStatus.CANCELLED, returned.getStatus());
        assertEquals(user, returned.getUser());
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void givenExistingOrderId_whenGetById_thenReturnOrder() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(100))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        Order fetchedOrder = orderService.getById(orderId);

        // Then
        assertNotNull(fetchedOrder);
        assertEquals(orderId, fetchedOrder.getId());
        assertEquals(OrderStatus.PENDING, fetchedOrder.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void givenNonExistingOrderId_whenGetById_thenThrowException() {
        // Given
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderDoesNotExistException.class, () -> orderService.getById(orderId));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void givenValidOrder_whenMakeOrder_thenOrderStatusUpdatedAndPaymentMade() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(100.00))
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();

        PaymentBankTransferRequest paymentBankTransferRequest = new PaymentBankTransferRequest();
        PaymentCreditCardRequest paymentCreditCardRequest = new PaymentCreditCardRequest();
        PaymentPayPalRequest payPalRequest = new PaymentPayPalRequest();

        Payment payment = Payment.builder().id(UUID.randomUUID()).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentService.createPayment(order, paymentBankTransferRequest, paymentCreditCardRequest, payPalRequest))
                .thenReturn(payment);
        doNothing().when(userService).clearShoppingCart(order.getUser());
        when(orderRepository.save(order)).thenReturn(order);
        doNothing().when(recordService).updateRecordsQuantityAfterOrder(order.getOrderInfos());
        Statistics statisticsForToday = new Statistics();
        statisticsForToday.setTotalMoney(BigDecimal.ZERO);
        statisticsForToday.setPendingOrders(1);
        when(statisticService.getStatisticsForToday()).thenReturn(statisticsForToday);

        // When
        Order confirmedOrder = orderService.makeOrder(orderId, paymentBankTransferRequest, paymentCreditCardRequest, payPalRequest);

        // Then
        assertNotNull(confirmedOrder);
        assertEquals(OrderStatus.CONFIRMED, confirmedOrder.getStatus());
        assertNotNull(confirmedOrder.getPayment());
        assertEquals(payment, confirmedOrder.getPayment());
        assertEquals(0, statisticsForToday.getPendingOrders());
        assertEquals(statisticsForToday.getTotalMoney(), BigDecimal.valueOf(100.00));
        verify(userService, times(1)).clearShoppingCart(order.getUser());
        verify(orderRepository, times(1)).save(confirmedOrder);
        verify(recordService, times(1)).updateRecordsQuantityAfterOrder(order.getOrderInfos());
        verify(statisticService, times(1)).save(statisticsForToday);
    }

    @Test
    void givenNonExistingOrder_whenMakeOrder_thenThrowException() {
        // Given
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderDoesNotExistException.class, () ->
                orderService.makeOrder(orderId, new PaymentBankTransferRequest(), new PaymentCreditCardRequest(), new PaymentPayPalRequest()));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void givenUserWithPendingOrders_whenGetPendingOrdersByGivenUser_thenReturnOnlyPendingOrders() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).build();
        Order pendingOrder1 = Order.builder().id(UUID.randomUUID()).status(OrderStatus.PENDING).build();
        Order pendingOrder2 = Order.builder().id(UUID.randomUUID()).status(OrderStatus.PENDING).build();
        Order confirmedOrder = Order.builder().id(UUID.randomUUID()).status(OrderStatus.CONFIRMED).build();

        List<Order> orders = Arrays.asList(pendingOrder1, pendingOrder2, confirmedOrder);
        user.setOrders(orders);

        // When
        List<Order> pendingOrders = orderService.getPendingOrdersByGivenUser(user);

        // Then
        assertNotNull(pendingOrders);
        assertEquals(2, pendingOrders.size());
        assertTrue(pendingOrders.contains(pendingOrder1));
        assertTrue(pendingOrders.contains(pendingOrder2));
        assertFalse(pendingOrders.contains(confirmedOrder));
    }

    @Test
    void givenUserWithNoPendingOrders_whenGetPendingOrdersByGivenUser_thenReturnEmptyList() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).build();

        Order confirmedOrder1 = Order.builder().id(UUID.randomUUID()).status(OrderStatus.CONFIRMED).build();
        Order confirmedOrder2 = Order.builder().id(UUID.randomUUID()).status(OrderStatus.CONFIRMED).build();

        List<Order> orders = Arrays.asList(confirmedOrder1, confirmedOrder2);
        user.setOrders(orders);

        // When
        List<Order> pendingOrders = orderService.getPendingOrdersByGivenUser(user);

        // Then
        assertNotNull(pendingOrders);
        assertTrue(pendingOrders.isEmpty());
    }

    @Test
    void givenUserWithEmptyOrderList_whenGetPendingOrdersByGivenUser_thenReturnEmptyList() {
        // Given
        User user = User.builder().id(UUID.randomUUID()).build();

        user.setOrders(Collections.emptyList());

        // When
        List<Order> pendingOrders = orderService.getPendingOrdersByGivenUser(user);

        // Then
        assertNotNull(pendingOrders);
        assertTrue(pendingOrders.isEmpty());
    }

    @Test
    void givenValidOrderIdAndShippingRequest_whenUpdateDataOfExistingOrder_thenOrderIsUpdated() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order existingOrder = Order.builder().id(orderId).firstName("Old First Name").lastName("Old Last Name").build();

        OrderShippingRequest shippingRequest = new OrderShippingRequest();
        shippingRequest.setOrderId(orderId);
        shippingRequest.setStreet("New Street");
        shippingRequest.setCity("New City");
        shippingRequest.setState("New State");
        shippingRequest.setZip("12345");
        shippingRequest.setFirstName("New First Name");
        shippingRequest.setLastName("New Last Name");
        shippingRequest.setEmail("new.email@example.com");
        shippingRequest.setPhone("123-456-7890");

        Address savedAddress = Address.builder().street("New Street").city("New City").state("New State").zip("12345").build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(addressService.save(anyString(), anyString(), anyString(), anyString())).thenReturn(savedAddress);
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        // When
        Order updatedOrder = orderService.updateDataOfExistingOrder(orderId, shippingRequest);

        // Then
        assertNotNull(updatedOrder);
        assertEquals("New First Name", updatedOrder.getFirstName());
        assertEquals("New Last Name", updatedOrder.getLastName());
        assertEquals("new.email@example.com", updatedOrder.getEmail());
        assertEquals("123-456-7890", updatedOrder.getPhone());
        assertEquals("New Street", updatedOrder.getAddress().getStreet());
        assertEquals("New City", updatedOrder.getAddress().getCity());
        assertEquals("New State", updatedOrder.getAddress().getState());
        assertEquals("12345", updatedOrder.getAddress().getZip());

        verify(orderRepository, times(1)).save(updatedOrder);
    }

    @Test
    void givenNonExistingOrderId_whenUpdateDataOfExistingOrder_thenReturnNull() {
        // Given
        UUID nonExistingOrderId = UUID.randomUUID();
        OrderShippingRequest shippingRequest = new OrderShippingRequest();
        shippingRequest.setOrderId(nonExistingOrderId);

        when(orderRepository.findById(nonExistingOrderId)).thenReturn(Optional.empty());

        // When
        Order updatedOrder = orderService.updateDataOfExistingOrder(nonExistingOrderId, shippingRequest);

        // Then
        assertNull(updatedOrder);
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void givenNullOrderIdInShippingRequest_whenUpdateDataOfExistingOrder_thenReturnNull() {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderShippingRequest shippingRequest = new OrderShippingRequest();
        shippingRequest.setOrderId(null);

        // When
        Order updatedOrder = orderService.updateDataOfExistingOrder(orderId, shippingRequest);

        // Then
        assertNull(updatedOrder);
        verify(orderRepository, times(0)).findById(any(UUID.class));
    }

    @Test
    void givenOrdersWithMultipleQuantities_whenGetTotalQuantityByGivenOrders_thenCorrectQuantitiesReturned() {
        // Given
        OrderInfo orderInfo1 = OrderInfo.builder().quantity(2).build();
        OrderInfo orderInfo2 = OrderInfo.builder().quantity(3).build();
        OrderInfo orderInfo3 = OrderInfo.builder().quantity(1).build();

        Order order1 = Order.builder().orderInfos(Arrays.asList(orderInfo1, orderInfo2)).build();
        Order order2 = Order.builder().orderInfos(Collections.singletonList(orderInfo3)).build();

        List<Order> orders = Arrays.asList(order1, order2);

        // When
        List<Integer> totalQuantities = orderService.getTotalQuantityByGivenOrders(orders);

        // Then
        assertEquals(2 + 3, totalQuantities.get(0));
        assertEquals(1, totalQuantities.get(1));
    }

    @Test
    void givenEmptyOrderList_whenGetTotalQuantityByGivenOrders_thenEmptyListReturned() {
        // Given
        List<Order> orders = new ArrayList<>();

        // When
        List<Integer> totalQuantities = orderService.getTotalQuantityByGivenOrders(orders);

        // Then
        assertTrue(totalQuantities.isEmpty());
    }

    @Test
    void givenOrdersWithZeroQuantity_whenGetTotalQuantityByGivenOrders_thenCorrectQuantitiesReturned() {
        // Given
        OrderInfo orderInfo1 = OrderInfo.builder().quantity(0).build();
        OrderInfo orderInfo2 = OrderInfo.builder().quantity(0).build();

        Order order1 = Order.builder().orderInfos(Arrays.asList(orderInfo1, orderInfo2)).build();
        Order order2 = Order.builder().orderInfos(Collections.singletonList(orderInfo1)).build();

        List<Order> orders = Arrays.asList(order1, order2);

        // When
        List<Integer> totalQuantities = orderService.getTotalQuantityByGivenOrders(orders);

        // Then
        assertEquals(0, totalQuantities.get(0));
        assertEquals(0, totalQuantities.get(1));
    }

    @Test
    void givenOrdersWithDifferentQuantityValues_whenGetTotalQuantityByGivenOrders_thenCorrectQuantitiesReturned() {
        // Given
        OrderInfo orderInfo1 = OrderInfo.builder().quantity(5).build();
        OrderInfo orderInfo2 = OrderInfo.builder().quantity(10).build();
        OrderInfo orderInfo3 = OrderInfo.builder().quantity(7).build();
        OrderInfo orderInfo4 = OrderInfo.builder().quantity(2).build();

        Order order1 = Order.builder().orderInfos(Arrays.asList(orderInfo1, orderInfo2)).build();
        Order order2 = Order.builder().orderInfos(Arrays.asList(orderInfo3, orderInfo4)).build();

        List<Order> orders = Arrays.asList(order1, order2);

        // When
        List<Integer> totalQuantities = orderService.getTotalQuantityByGivenOrders(orders);

        // Then
        assertEquals(5 + 10, totalQuantities.get(0));
        assertEquals(7 + 2, totalQuantities.get(1));
    }

    @Test
    void givenOrder_whenSave_thenOrderSaved() {
        // Given
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(100))
                .build();

        when(orderRepository.save(order)).thenReturn(order);  // Mock the save method

        // When
        Order savedOrder = orderService.save(order);

        // Then
        assertNotNull(savedOrder);
        assertEquals(order.getId(), savedOrder.getId());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void givenPageRequestAndFilter_whenGetEightOrders_thenCorrectPageReturned() {
        // Given
        OrderFilterRequest filterRequest = OrderFilterRequest.builder().productsName("Test").build();
        filterRequest.setOrderStatus(OrderStatus.PENDING);

        Order order1 = Order.builder().status(OrderStatus.PENDING).totalPrice(BigDecimal.valueOf(100)).build();
        Order order2 = Order.builder().status(OrderStatus.PENDING).totalPrice(BigDecimal.valueOf(200)).build();
        List<Order> orders = Arrays.asList(order1, order2);

        Page<Order> page = new PageImpl<>(orders);
        PageRequest pageRequest = PageRequest.of(0, 8);

        when(orderRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(page);

        // When
        Page<Order> result = orderService.getEightOrders(pageRequest, filterRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(OrderStatus.PENDING, result.getContent().get(0).getStatus());
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    void givenPageRequestAndFilterEmptyName_whenGetEightOrders_thenCorrectPageReturned() {
        // Given
        OrderFilterRequest filterRequest = OrderFilterRequest.builder().productsName(" ").build();
        filterRequest.setOrderStatus(OrderStatus.PENDING);

        Order order1 = Order.builder().status(OrderStatus.PENDING).totalPrice(BigDecimal.valueOf(100)).build();
        Order order2 = Order.builder().status(OrderStatus.PENDING).totalPrice(BigDecimal.valueOf(200)).build();
        List<Order> orders = Arrays.asList(order1, order2);

        Page<Order> page = new PageImpl<>(orders);
        PageRequest pageRequest = PageRequest.of(0, 8);

        when(orderRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(page);

        // When
        Page<Order> result = orderService.getEightOrders(pageRequest, filterRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(OrderStatus.PENDING, result.getContent().get(0).getStatus());
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    void givenOrderFilterRequest_whenGetEightOrders_thenReturnFilteredOrders() {
        // Given
        OrderFilterRequest filterRequest = new OrderFilterRequest();
        filterRequest.setProductsName("book");
        filterRequest.setOrderStatus(OrderStatus.PENDING);
        filterRequest.setFromDate(LocalDate.of(2023, 1, 1));
        filterRequest.setToDate(LocalDate.of(2023, 1, 31));

        Specification<Order> mockSpec = mock(Specification.class);

        Order order1 = Order.builder().id(UUID.randomUUID()).status(OrderStatus.PENDING).build();
        Order order2 = Order.builder().id(UUID.randomUUID()).status(OrderStatus.PENDING).build();
        Page<Order> mockedPage = new PageImpl<>(Arrays.asList(order1, order2));

        when(orderRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockedPage);

        // When
        Page<Order> result = orderService.getEightOrders(PageRequest.of(0, 8), filterRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void givenDateRange_whenGetOrdersByDay_thenReturnFilteredOrders() {
        // Given
        LocalDateTime from = LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2023, 1, 31, 23, 59, 59, 999999);

        Order order1 = Order.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.of(2023, 1, 15, 10, 0, 0, 0)).build();
        Order order2 = Order.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.of(2023, 1, 25, 15, 0, 0, 0)).build();
        Order order3 = Order.builder().id(UUID.randomUUID()).createdAt(LocalDateTime.of(2023, 2, 1, 9, 0, 0, 0)).build();

        List<Order> mockOrders = Arrays.asList(order1, order2);
        when(orderRepository.findAllByCreatedAtBetween(from, to)).thenReturn(mockOrders);

        // When
        List<Order> result = orderService.getOrdersByDay(from, to);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(order1));
        assertTrue(result.contains(order2));
        assertFalse(result.contains(order3));

        verify(orderRepository, times(1)).findAllByCreatedAtBetween(from, to);
    }

    @Test
    void givenWeekPeriod_whenGetChartInfo_thenReturnWeeklyChartData() {
        // Given
        // When
        Map<String, Integer> result = orderService.getChartInfo("week");

        // Then
        assertNotNull(result);
        assertEquals(7, result.size());
        assertTrue(result.containsKey("Mon"));
        assertTrue(result.containsKey("Sun"));
    }

    @Test
    void givenMonthPeriod_whenGetChartInfo_thenReturnMonthlyChartData() {
        // Given
        // When
        Map<String, Integer> result = orderService.getChartInfo("month");

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue(result.containsKey("Apr 7 - Apr 13"));
    }

    @Test
    void givenYearPeriod_whenGetChartInfo_thenReturnYearlyChartData() {
        // Given
        // When
        Map<String, Integer> result = orderService.getChartInfo("year");

        // Then
        assertNotNull(result);
        assertEquals(12, result.size());
        assertTrue(result.containsKey("JANUARY"));
        assertTrue(result.containsKey("FEBRUARY"));
    }

    @Test
    void givenPeriodThatDoesNotExist_whenGetChartInfo_thenReturnNull() {
        // Given
        // When
        Map<String, Integer> result = orderService.getChartInfo("test");

        // Then
        assertNull(result);
    }

}
