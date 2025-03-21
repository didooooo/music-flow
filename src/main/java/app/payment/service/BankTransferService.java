package app.payment.service;

import app.payment.model.BankTransfer;
import app.payment.repository.BankTransferRepository;
import app.web.dto.PaymentBankTransferRequest;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankTransferService {
    private final BankTransferRepository bankTransferRepository;
    private final ConversionService conversionService;

    public BankTransferService(BankTransferRepository bankTransferRepository, ConversionService conversionService) {
        this.bankTransferRepository = bankTransferRepository;
        this.conversionService = conversionService;
    }

    public BankTransfer createNewBankTransferAccount(PaymentBankTransferRequest paymentBankTransferRequest) {
        Optional<BankTransfer> bankTransfer = bankTransferRepository.findByIban(paymentBankTransferRequest.getIban());
        if (bankTransfer.isPresent()) {
            return bankTransfer.get();
        }
        BankTransfer converted = conversionService.convert(paymentBankTransferRequest, BankTransfer.class);
        return bankTransferRepository.save(converted);
    }
}
