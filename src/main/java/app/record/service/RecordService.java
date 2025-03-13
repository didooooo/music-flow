package app.record.service;

import app.record.model.Record;
import app.record.repository.RecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordService {
    private final RecordRepository recordRepository;

    public RecordService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public List<Record> getAllRecords() {
        return recordRepository.findAll();
    }
}
