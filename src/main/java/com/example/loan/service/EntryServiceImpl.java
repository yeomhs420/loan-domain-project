package com.example.loan.service;

import com.example.loan.domain.Application;
import com.example.loan.domain.Entry;
import com.example.loan.dto.BalanceDTO;
import com.example.loan.dto.EntryDTO.Request;
import com.example.loan.dto.EntryDTO.Response;
import com.example.loan.dto.EntryDTO.UpdateResponse;
import com.example.loan.exception.BaseException;
import com.example.loan.exception.ResultType;
import com.example.loan.repository.ApplicationRepository;
import com.example.loan.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntryServiceImpl implements EntryService {

  private final EntryRepository entryRepository;

  private final ApplicationRepository applicationRepository;

  private final BalanceService balanceService;

  private final ModelMapper modelMapper;

  @Transactional
  @Override
  public Response create(Long applicationId, Request request) {
    if (!isContractedApplication(applicationId)) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    Entry entry = modelMapper.map(request, Entry.class);
    entry.setApplicationId(applicationId);

    entryRepository.save(entry);

    balanceService.create(applicationId,
        BalanceDTO.CreateRequest.builder()
            .entryAmount(request.getEntryAmount())
            .build()
    );

    return modelMapper.map(entry, Response.class);
  }


  @Override
  public Response get(Long applicationId) {
    Entry entry = entryRepository.findByApplicationId(applicationId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    return modelMapper.map(entry, Response.class);
  }

  @Transactional
  @Override
  public UpdateResponse update(Long entryId, Request request) {
    Entry entry = entryRepository.findById(entryId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    BigDecimal beforeEntryAmount = entry.getEntryAmount();
    entry.setEntryAmount(request.getEntryAmount());

    entryRepository.save(entry);

    Long applicationId = entry.getApplicationId();
    balanceService.update(applicationId,
        BalanceDTO.UpdateRequest.builder()
            .beforeEntryAmount(beforeEntryAmount)
            .afterEntryAmount(request.getEntryAmount())
            .build()
    );

    return UpdateResponse.builder()
        .applicationId(entry.getApplicationId())
        .beforeEntryAmount(beforeEntryAmount)
        .afterEntryAmount(entry.getEntryAmount())
        .build();
  }


  @Transactional
  @Override
  public void delete(Long entryId) {
    Entry entry = entryRepository.findById(entryId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    entry.setIsDeleted(true);

    entryRepository.save(entry);

    BigDecimal beforeEntryAmount = entry.getEntryAmount();

    Long applicationId = entry.getApplicationId();
    balanceService.update(applicationId,
        BalanceDTO.UpdateRequest.builder()
            .beforeEntryAmount(beforeEntryAmount)
            .afterEntryAmount(BigDecimal.ZERO)
            .build()
    );
  }

  private boolean isContractedApplication(Long applicationId) {
    Optional<Application> existed = applicationRepository.findById(applicationId);
    if (existed.isEmpty()) {
      return false;
    }

    return existed.get().getContractedAt() != null;
  }
}
