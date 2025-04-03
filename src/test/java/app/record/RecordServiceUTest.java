package app.record;

import app.artist.model.Artist;
import app.artist.service.ArtistService;
import app.exception.RecordNotFoundException;
import app.order.model.Order;
import app.order.model.OrderInfo;
import app.order.service.OrderService;
import app.record.model.Format;
import app.record.model.Genre;
import app.record.model.Type;
import app.record.repository.RecordRepository;
import app.record.service.RecordService;
import app.review.model.Review;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.web.dto.RecordUpsertRequest;
import app.web.dto.enums.FormatRequest;
import app.web.dto.enums.GenreRequest;
import app.web.dto.enums.TypeRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import app.record.model.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class RecordServiceUTest {
    @Mock
    private RecordRepository recordRepository;
    @Mock
    private ConversionService conversionService;
    @Mock
    private ArtistService artistService;
    @Mock
    private StatisticService statisticService;
    @Mock
    private OrderService orderService;
    @InjectMocks
    private RecordService recordService;

    @Test
    void whenGetAllRecords_thenReturnAllRecords() {
        // Given
        List<Record> records = List.of(new Record(), new Record());
        when(recordRepository.findAll()).thenReturn(records);

        // When
        List<Record> result = recordService.getAllRecords();

        // Then
        assertEquals(records, result);
        verify(recordRepository, times(1)).findAll();
    }

    @Test
    void whenGetAllRecords_thenReturnEmptyList() {
        // Given
        when(recordRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<Record> result = recordService.getAllRecords();

        // Then
        assertEquals(0, result.size());
        verify(recordRepository, times(1)).findAll();
    }

    @Test
    void givenExistingRecord_whenUpsertRecord_thenUpdateRecord() {
        // Given
        RecordUpsertRequest input = new RecordUpsertRequest();
        input.setId(UUID.randomUUID());
        input.setDescription("Updated Description");
        input.setArtistsInput("Tate McRae, Kendrick Lamar");
        input.setFormat(FormatRequest.CD);
        input.setGenre(GenreRequest.CLASSICAL);
        input.setType(TypeRequest.ALBUM);
        Record existingRecord = Record.builder().build();
        Artist artist = Artist.builder()
                .name("Tate McRae")
                .records(List.of(existingRecord))
                .id(UUID.randomUUID()).build();
        existingRecord.setArtists(List.of(artist));
        when(recordRepository.findById(input.getId())).thenReturn(Optional.of(existingRecord));
        when(recordRepository.save(any(Record.class))).thenReturn(existingRecord);
        when(artistService.save(any())).thenReturn(artist);

        // When
        recordService.upsertRecord(input);

        // Then
        verify(recordRepository, times(1)).save(existingRecord);
    }

    @Test
    void givenExistingRecordWithPhotoUrl_whenUpsertRecord_thenUpdateRecord() {
        // Given
        RecordUpsertRequest input = new RecordUpsertRequest();
        input.setId(UUID.randomUUID());
        input.setDescription("Updated Description");
        input.setArtistsInput("Tate McRae, Kendrick Lamar");
        input.setFormat(FormatRequest.CD);
        input.setRecordCover("www.recordcover.com");
        input.setGenre(GenreRequest.CLASSICAL);
        input.setType(TypeRequest.ALBUM);
        Record existingRecord = Record.builder().build();
        Artist artist = Artist.builder()
                .name("Tate McRae")
                .records(List.of(existingRecord))
                .id(UUID.randomUUID()).build();
        existingRecord.setArtists(List.of(artist));
        when(recordRepository.findById(input.getId())).thenReturn(Optional.of(existingRecord));
        when(recordRepository.save(any(Record.class))).thenReturn(existingRecord);
        when(artistService.save(any())).thenReturn(artist);

        // When
        recordService.upsertRecord(input);

        // Then
        assertEquals("www.recordcover.com", existingRecord.getImage());
        verify(recordRepository, times(1)).save(existingRecord);
    }

    @Test
    void givenExistingRecordWithExistingArtists_whenUpsertRecord_thenUpdateRecord() {
        // Given
        RecordUpsertRequest input = new RecordUpsertRequest();
        input.setId(UUID.randomUUID());
        input.setDescription("Updated Description");
        input.setArtistsInput("Tate McRae, Kendrick Lamar");
        input.setFormat(FormatRequest.CD);
        input.setRecordCover("www.recordcover.com");
        input.setGenre(GenreRequest.CLASSICAL);
        input.setType(TypeRequest.ALBUM);
        Record existingRecord = Record.builder().build();
        Artist artist = Artist.builder()
                .name("Test")
                .records(List.of(existingRecord))
                .id(UUID.randomUUID()).build();
        existingRecord.setArtists(new ArrayList<>());
        existingRecord.getArtists().add(Artist.builder().id(UUID.randomUUID()).name("Test2").build());
        when(recordRepository.findById(input.getId())).thenReturn(Optional.of(existingRecord));
        when(recordRepository.save(any(Record.class))).thenReturn(existingRecord);
        when(artistService.save(any())).thenReturn(artist);

        // When
        recordService.upsertRecord(input);

        // Then
        assertEquals("www.recordcover.com", existingRecord.getImage());
        verify(recordRepository, times(1)).save(existingRecord);
    }

    @Test
    void givenNonExistingRecord_whenUpsertRecord_thenUpdateRecord() {
        // Given
        RecordUpsertRequest input = new RecordUpsertRequest();
        input.setDescription("Updated Description");
        input.setArtistsInput("Tate McRae, Kendrick Lamar");
        input.setFormat(FormatRequest.CD);
        input.setGenre(GenreRequest.CLASSICAL);
        input.setType(TypeRequest.ALBUM);
        Record newRecord = Record.builder()
                .description(input.getDescription())
                .format(Format.getByCode(input.getFormat().name()))
                .genre(Genre.getByCode(input.getGenre().name()))
                .id(UUID.randomUUID())
                .type(Type.getByCode(input.getType().name()))
                .build();
        Artist artist = Artist.builder()
                .name("Tate McRae")
                .records(List.of(newRecord))
                .id(UUID.randomUUID()).build();
        newRecord.setArtists(List.of(artist));
        Statistics statistics = Statistics.builder()
                .activeUsers(1)
                .inactiveUsers(0)
                .date(LocalDate.now())
                .totalRecords(1)
                .totalOrders(1)
                .totalCustomers(1)
                .pendingOrders(0)
                .totalMoney(BigDecimal.TEN)
                .shippedOrders(0)
                .build();
        when(recordRepository.save(any())).thenReturn(newRecord);
        when(artistService.save(any())).thenReturn(artist);
        when(conversionService.convert(input, Record.class)).thenReturn(newRecord);
        when(statisticService.getStatisticsForToday()).thenReturn(statistics);
        // When
        recordService.upsertRecord(input);

        // Then
        assertNotEquals(newRecord.getId(), input.getId());
        assertEquals(newRecord.getDescription(), input.getDescription());
        assertEquals(2, statistics.getTotalRecords());
        verify(recordRepository, times(1)).save(any());
    }

    @Test
    void givenValidId_whenFindById_thenReturnRecord() {
        // Given
        UUID recordId = UUID.randomUUID();
        Record record = new Record();
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        // When
        Record result = recordService.findById(recordId);

        // Then
        assertEquals(record, result);
        verify(recordRepository).findById(recordId);
    }

    @Test
    void givenInvalidId_whenFindById_thenThrowException() {
        // Given
        UUID recordId = UUID.randomUUID();
        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RecordNotFoundException.class, () -> recordService.findById(recordId));
    }

    @Test
    void whenGetEightRecordsByName_thenReturnPageOfRecords() {
        // Given
        String name = "Test Record";
        PageRequest pageRequest = PageRequest.of(0, 8);
        List<Record> records = List.of(new Record(),
                new Record(),
                new Record(),
                new Record(),
                new Record(),
                new Record(),
                new Record(),
                new Record());
        Page<Record> page = new PageImpl<>(records);
        when(recordRepository.findAllByTitleContainingIgnoreCase(name, pageRequest)).thenReturn(page);

        // When
        Page<Record> result = recordService.getEightRecordsByName(pageRequest, name);

        // Then
        assertEquals(page, result);
        assertEquals(records.size(), result.getTotalElements());
        verify(recordRepository).findAllByTitleContainingIgnoreCase(name, pageRequest);
    }

    @Test
    void whenGetEightRecordsByName_thenReturnPageOfRecordsButWithLessPageSize() {
        // Given
        String name = "Test Record";
        PageRequest pageRequest = PageRequest.of(0, 8);
        List<Record> records = List.of(new Record(),
                new Record(),
                new Record());
        Page<Record> page = new PageImpl<>(records);
        when(recordRepository.findAllByTitleContainingIgnoreCase(name, pageRequest)).thenReturn(page);

        // When
        Page<Record> result = recordService.getEightRecordsByName(pageRequest, name);

        // Then
        assertEquals(page, result);
        assertEquals(records.size(), result.getTotalElements());
        assertEquals(3, result.getTotalElements());
        assertNotEquals(8, result.getTotalElements());
        verify(recordRepository).findAllByTitleContainingIgnoreCase(name, pageRequest);
    }

    @Test
    void whenAddReviewToTheRecord_thenSaveRecord() {
        // Given
        Record record = new Record();
        record.setReviews(new ArrayList<>());
        Review review = new Review();
        when(recordRepository.save(record)).thenReturn(record);

        // When
        recordService.addReviewToTheRecord(record, review);

        // Then
        assertTrue(record.getReviews().contains(review));
        verify(recordRepository).save(record);
    }

    @Test
    void whenGetNewestRecords_thenReturnTop4Records() {
        // Given
        List<Record> records = List.of(new Record(), new Record(), new Record(), new Record());
        when(recordRepository.findTop4ByOrderByReleaseDateDesc()).thenReturn(records);

        // When
        List<Record> result = recordService.getNewestRecords();

        // Then
        assertEquals(4, result.size());
        verify(recordRepository).findTop4ByOrderByReleaseDateDesc();
    }

    @Test
    void givenRecord_whenUpdateRecordsQuantityAfterOrder_thenSaveRecords() {
        // Given
        Record record = new Record();
        record.setQuantity(10);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setRecord(record);
        orderInfo.setQuantity(2);
        List<OrderInfo> orderInfos = List.of(orderInfo);
        when(recordRepository.save(record)).thenReturn(record);

        // When
        recordService.updateRecordsQuantityAfterOrder(orderInfos);

        // Then
        assertEquals(8, record.getQuantity());
        verify(recordRepository).save(record);
    }

    @Test
    void givenRecord_whenDeleteRecord_thenUpdateStatistics() {
        // Given
        Record record = new Record();
        record.setQuantity(5);
        Statistics statistics = new Statistics();
        statistics.setTotalRecords(10);
        when(statisticService.getStatisticsForToday()).thenReturn(statistics);
        when(recordRepository.save(record)).thenReturn(record);

        // When
        recordService.deleteRecord(record);

        // Then
        assertEquals(0, record.getQuantity());
        assertEquals(9, statistics.getTotalRecords());
        verify(recordRepository).save(record);
        verify(statisticService).save(statistics);
    }

    @Test
    void whenGetTopSellingRecord_thenReturnMostSoldRecord() {
        // Given
        Record record1 = new Record();
        Record record2 = new Record();
        OrderInfo orderInfo1 = OrderInfo.builder().record(record1).quantity(5).build();
        OrderInfo orderInfo2 = OrderInfo.builder().quantity(10).record(record2).build();
        Order order = Order.builder().orderInfos(new ArrayList<>(List.of(orderInfo1, orderInfo2))).build();
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        // When
        Record result = recordService.getTopSellingRecord();

        // Then
        assertEquals(record2, result);
        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void whenGetTopSellingRecord_thenReturnNull() {
        // Given

        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());

        // When
        Record result = recordService.getTopSellingRecord();

        // Then
        assertNull(result);
        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void givenRecord_whenGetTotalSoldQuantityAndTotalMoneySpent_thenReturnCorrectValues() {
        // Given
        Record record = new Record();
        record.setId(UUID.randomUUID());
        record.setPrice(BigDecimal.TEN);
        OrderInfo orderInfo = OrderInfo.builder().record(record).quantity(3).build();
        Order order = Order.builder().orderInfos(new ArrayList<>(List.of(orderInfo))).build();
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        // When
        Pair<BigDecimal, Integer> result = recordService.getTotalSoldQuantityAndTotalMoneySpent(record);

        // Then
        assertEquals(BigDecimal.valueOf(30), result.getFirst());
        assertEquals(3, result.getSecond().intValue());
        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void givenRecordThatDoesNotExistInAllOrders_whenGetTotalSoldQuantityAndTotalMoneySpent_thenReturnCorrectValues() {
        // Given
        Record record = new Record();
        record.setId(UUID.randomUUID());
        record.setPrice(BigDecimal.TEN);
        Record record2 = new Record();
        record.setId(UUID.randomUUID());
        record.setPrice(BigDecimal.TEN);
        OrderInfo orderInfo = OrderInfo.builder().record(record).quantity(3).build();
        Order order = Order.builder().orderInfos(new ArrayList<>(List.of(orderInfo))).build();
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        // When
        Pair<BigDecimal, Integer> result = recordService.getTotalSoldQuantityAndTotalMoneySpent(record2);

        // Then
        assertEquals(BigDecimal.ZERO, result.getFirst());
        assertEquals(0, result.getSecond().intValue());
        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void givenEmptyOrderList_whenGetTotalSoldQuantityAndTotalMoneySpent_thenReturnCorrectValues() {
        // Given
        Record record = new Record();
        when(orderService.getAllOrders()).thenReturn(new ArrayList<>());

        // When
        Pair<BigDecimal, Integer> result = recordService.getTotalSoldQuantityAndTotalMoneySpent(record);

        // Then
        assertEquals(BigDecimal.ZERO, result.getFirst());
        assertEquals(0, result.getSecond().intValue());
        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void givenPageRequestAndSortPriceAsc_whenGetRecordsWithGivenSize_thenReturnSortedRecords() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Record> records = List.of(new Record(), new Record());
        Page<Record> page = new PageImpl<>(records);

        when(recordRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When
        Page<Record> result = recordService.getRecordsWithGivenSize(pageRequest, "priceAsc");

        // Then
        assertEquals(page, result);
        verify(recordRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price")));
    }

    @Test
    void givenPageRequestAndSortPriceDesc_whenGetRecordsWithGivenSize_thenReturnSortedRecords() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Record> records = List.of(new Record(), new Record());
        Page<Record> page = new PageImpl<>(records);

        when(recordRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When
        Page<Record> result = recordService.getRecordsWithGivenSize(pageRequest, "priceDesc");

        // Then
        assertEquals(page, result);
        verify(recordRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price")));
    }

    @Test
    void givenPageRequestAndSortReleaseDate_whenGetRecordsWithGivenSize_thenReturnSortedRecords() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Record> records = List.of(new Record(), new Record());
        Page<Record> page = new PageImpl<>(records);

        when(recordRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When
        Page<Record> result = recordService.getRecordsWithGivenSize(pageRequest, "releaseDate");

        // Then
        assertEquals(page, result);
        verify(recordRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "releaseDate")));
    }

    @Test
    void givenPageRequestAndSortTitleAsc_whenGetRecordsWithGivenSize_thenReturnSortedRecords() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Record> records = List.of(new Record(), new Record());
        Page<Record> page = new PageImpl<>(records);

        when(recordRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When
        Page<Record> result = recordService.getRecordsWithGivenSize(pageRequest, "nameAsc");

        // Then
        assertEquals(page, result);
        verify(recordRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title")));
    }

    @Test
    void givenPageRequestAndSortTitleDesc_whenGetRecordsWithGivenSize_thenReturnSortedRecords() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Record> records = List.of(new Record(), new Record());
        Page<Record> page = new PageImpl<>(records);

        when(recordRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When
        Page<Record> result = recordService.getRecordsWithGivenSize(pageRequest, "nameDesc");

        // Then
        assertEquals(page, result);
        verify(recordRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title")));
    }

    @Test
    void givenPageRequestAndSortNull_whenGetRecordsWithGivenSize_thenReturnSortedRecords() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Record> records = List.of(new Record(), new Record());
        Page<Record> page = new PageImpl<>(records);

        when(recordRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When
        Page<Record> result = recordService.getRecordsWithGivenSize(pageRequest, null);

        // Then
        assertEquals(page, result);
        verify(recordRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void givenPageRequestAndSortThatIsNotInTheChoices_whenGetRecordsWithGivenSize_thenReturnSortedRecords() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Record> records = List.of(new Record(), new Record());
        Page<Record> page = new PageImpl<>(records);

        when(recordRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // When
        Page<Record> result = recordService.getRecordsWithGivenSize(pageRequest, "test");

        // Then
        assertEquals(page, result);
        verify(recordRepository, times(1)).findAll(pageRequest);
    }
}
