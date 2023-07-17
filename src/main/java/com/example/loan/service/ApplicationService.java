package com.example.loan.service;

import com.example.loan.dto.ApplicationDTO.AcceptTerms;
import com.example.loan.dto.ApplicationDTO.Request;
import com.example.loan.dto.ApplicationDTO.Response;

public interface ApplicationService {

  Response create(Request request);

  Response get(Long applicationId);

  Response update(Long applicationId, Request request);

  void delete(Long applicationId);

  Boolean acceptTerms(Long applicationId, AcceptTerms request);

  Response contract(Long applicationId);
}
