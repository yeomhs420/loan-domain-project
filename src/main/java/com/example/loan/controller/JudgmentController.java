package com.example.loan.controller;

import com.example.loan.dto.ApplicationDTO.GrantAmount;
import com.example.loan.dto.JudgmentDTO.Request;
import com.example.loan.dto.JudgmentDTO.Response;
import com.example.loan.dto.ResponseDTO;
import com.example.loan.service.JudgmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/judgments")
public class JudgmentController extends AbstractController {

  private final JudgmentService judgmentService;

  @PostMapping
  public ResponseDTO<Response> create(@RequestBody Request request) {
    return ok(judgmentService.create(request));
  } // 신청 정보에 대한 심사 등록

  @GetMapping("/{judgmentId}")
  public ResponseDTO<Response> get(@PathVariable Long judgmentId) {
    return ok(judgmentService.get(judgmentId));
  }

  @PutMapping("/{judgmentId}")
  public ResponseDTO<Response> update(@PathVariable Long judgmentId, @RequestBody Request request) {
    return ok(judgmentService.update(judgmentId, request));
  }

  @DeleteMapping("/{judgmentId}")
  public ResponseDTO<Void> delete(@PathVariable Long judgmentId) {
    judgmentService.delete(judgmentId);
    return ok();
  }

  @PatchMapping("/{judgmentId}/grants") // 신청 등록 -> 심사 등록 -> 한도 부여
  public ResponseDTO<GrantAmount> grant(@PathVariable Long judgmentId) {
    return ok(judgmentService.grant(judgmentId));
  }
}
