package com.example.loan.controller;

import com.example.loan.dto.EntryDTO.Request;
import com.example.loan.dto.EntryDTO.Response;
import com.example.loan.dto.EntryDTO.UpdateResponse;
import com.example.loan.dto.RepaymentDTO;
import com.example.loan.dto.RepaymentDTO.ListResponse;
import com.example.loan.dto.ResponseDTO;
import com.example.loan.service.EntryService;
import com.example.loan.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/applications")
public class InternalController extends AbstractController {

  private final EntryService entryService;

  private final RepaymentService repaymentService;

  @PostMapping("{applicationId}/entries") // 집행
  public ResponseDTO<Response> create(@PathVariable Long applicationId, @RequestBody Request request) {
    return ok(entryService.create(applicationId, request));
  }

  @PutMapping("{applicationId}/entries")
  public ResponseDTO<UpdateResponse> update(@PathVariable Long applicationId, @RequestBody Request request) {
    return ok(entryService.update(applicationId, request));
  }

  @GetMapping("/entries/{entryId}")
  public ResponseDTO<Response> get(@PathVariable Long entryId) {
    return ok(entryService.get(entryId));
  }

  @DeleteMapping("/entries/{entryId}")
  public ResponseDTO<Void> delete(@PathVariable Long entryId) {
    entryService.delete(entryId);
    return ok();
  }

  @PostMapping("{applicationId}/repayments")  // 상환
  public ResponseDTO<RepaymentDTO.Response> create(@PathVariable Long applicationId, @RequestBody RepaymentDTO.Request request) {
    return ok(repaymentService.create(applicationId, request));
  }

  @GetMapping("{applicationId}/repayments")
  public ResponseDTO<List<ListResponse>> getPayments(@PathVariable Long applicationId) {
    return ok(repaymentService.get(applicationId));
  }

  @PutMapping("/repayments/{repaymentId}")
  public ResponseDTO<RepaymentDTO.UpdateResponse> update(@PathVariable Long repaymentId,
      @RequestBody RepaymentDTO.Request request) {
    return ok(repaymentService.update(repaymentId, request));
  }

  @DeleteMapping("/repayments/{repaymentId}")
  public ResponseDTO<Void> deleteRepayment(@PathVariable Long repaymentId) {
    repaymentService.delete(repaymentId);
    return ok();
  }
}
