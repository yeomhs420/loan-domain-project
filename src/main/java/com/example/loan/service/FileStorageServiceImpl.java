package com.example.loan.service;

import com.example.loan.exception.BaseException;
import com.example.loan.exception.ResultType;
import com.example.loan.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

  @Value("${spring.servlet.multipart.location}")
  private String uploadPath;

  private final ApplicationRepository applicationRepository;

  @Override
  public void save(Long applicationId, MultipartFile file) {  // 파일 업로드
    if (!isPresentApplication(applicationId)) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    try {
      String applicationPath = uploadPath.concat("/" + applicationId);
      Path directoryPath = Path.of(applicationPath);
      if (!Files.exists(directoryPath)) { // 해당 경로에 디렉토리가 없으면 생성
        Files.createDirectory(directoryPath);
      }

      Files.copy(file.getInputStream(), Paths.get(applicationPath).resolve(file.getOriginalFilename()));
    } catch (Exception e) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }
  }


  @Override
  public Resource load(Long applicationId, String filename) { // 특정 파일을 다운할 수 있도록 리소스를 리턴
    if (!isPresentApplication(applicationId)) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    try {
      String applicationPath = uploadPath.concat("/" + applicationId);
      Path file = Paths.get(applicationPath).resolve(filename); // 경로 설정과 경로에 파일명 추가
      Resource resource = new UrlResource(file.toUri());

      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new BaseException(ResultType.SYSTEM_ERROR);
      }
    } catch (MalformedURLException e ) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }
  }

  @Override
  public void deleteAll(Long applicationId) {
    if (!isPresentApplication(applicationId)) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    String applicationPath = uploadPath.concat("/" + applicationId);
    FileSystemUtils.deleteRecursively(Paths.get(applicationPath).toFile()); // 해당 경로의 모든 파일 삭제
  }

  @Override
  public Stream<Path> loadAll(Long applicationId) { // 특정 경로의 모든 파일을 리턴
    if (!isPresentApplication(applicationId)) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }

    try {
      String applicationPath = uploadPath.concat("/" + applicationId);

      return Files.walk(Paths.get(applicationPath), 1).filter(path -> !path.equals(Paths.get(applicationPath)));
      // 해당 경로부터 한 단계까지 파일 검색, 검색된 파일 중 기준 경로 제외한 파일들 필터링

    } catch (IOException e) {
      throw new BaseException(ResultType.SYSTEM_ERROR);
    }
  }

  private boolean isPresentApplication(Long applicationId) {
    return applicationRepository.findById(applicationId).isPresent();
  }
}
