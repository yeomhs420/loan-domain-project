package com.example.loan.service;

import com.example.loan.domain.AcceptTerms;
import com.example.loan.domain.Application;
import com.example.loan.domain.Terms;
import com.example.loan.dto.ApplicationDTO;
import com.example.loan.dto.ApplicationDTO.Request;
import com.example.loan.dto.ApplicationDTO.Response;
import com.example.loan.exception.BaseException;
import com.example.loan.exception.ResultType;
import com.example.loan.repository.AcceptTermsRepository;
import com.example.loan.repository.ApplicationRepository;
import com.example.loan.repository.JudgmentRepository;
import com.example.loan.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

  private final ApplicationRepository applicationRepository;

  private final TermsRepository termsRepository;

  private final AcceptTermsRepository acceptTermsRepository;

  private final JudgmentRepository judgmentRepository;

  private final ModelMapper modelMapper;

  @Override
  public Response create(Request request) {
    Application application = modelMapper.map(request, Application.class);
    application.setAppliedAt(LocalDateTime.now());

    Application applied = applicationRepository.save(application);

    return modelMapper.map(applied, Response.class);
  }

  @Override
  public Response get(Long applicationId) {
    Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    return modelMapper.map(application, Response.class);
  }

  @Override
  public Response update(Long applicationId, Request request) {
    Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    application.setName(request.getName());
    application.setCellPhone(request.getCellPhone());
    application.setEmail(request.getEmail());
    application.setHopeAmount(request.getHopeAmount());

    applicationRepository.save(application);

    return modelMapper.map(application, Response.class);
  }

  @Override
  public void delete(Long applicationId) {
    Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    application.setIsDeleted(true);

    applicationRepository.save(application);
  }

  @Override
  public Boolean acceptTerms(Long applicationId, ApplicationDTO.AcceptTerms dto) {  // 이용 약관 등록 여부
    applicationRepository.findById(applicationId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    List<Terms> termsList = termsRepository.findAll(Sort.by(Direction.ASC, "termsId"));
    if (termsList.isEmpty()) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    List<Long> acceptTermsIds = dto.getAcceptTermsIds();
    if (termsList.size() != acceptTermsIds.size()) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    List<Long> termsIds = termsList.stream().map(Terms::getTermsId).collect(Collectors.toList());
    Collections.sort(acceptTermsIds);

    if (!termsIds.containsAll(acceptTermsIds)) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    for (Long termsId : acceptTermsIds) {
      AcceptTerms accepted = AcceptTerms.builder()
          .termsId(termsId)
          .applicationId(applicationId)
          .build();

      acceptTermsRepository.save(accepted);
    } // 체크된 약관에 대한 리스트 정보들을 AcceptTerms entity 에 하나씩 매핑하여 save 처리

    return true;
  }


  @Transactional
  @Override
  public Response contract(Long applicationId) {
    Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    judgmentRepository.findByApplicationId(applicationId).orElseThrow(() -> {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    });

    if (application.getApprovalAmount() == null
        || application.getApprovalAmount().compareTo(BigDecimal.ZERO) == 0) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    application.setContractedAt(LocalDateTime.now());

    Application updated = applicationRepository.save(application);

    return modelMapper.map(updated, Response.class);
  }
}
