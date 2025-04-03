package app.order;

import app.order.model.OrderInfo;
import app.order.repository.OrderInfoRepository;
import app.order.service.OrderInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderInfoServiceUTest {
    @Mock
    private OrderInfoRepository orderInfoRepository;
    @InjectMocks
    private OrderInfoService orderInfoService;
    @Test
    void givenOrderInfo_whenSave_thenReturnSavedOrderInfo() {
        //Given
        UUID orderId = UUID.randomUUID();
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        //When
        when(orderInfoRepository.save(any())).thenReturn(orderInfo);
        //Then
        OrderInfo result = orderInfoService.save(orderInfo);
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderInfoRepository,times(1)).save(orderInfo);
    }
}
