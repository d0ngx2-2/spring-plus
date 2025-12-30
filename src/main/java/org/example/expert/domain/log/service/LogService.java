package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
// 매니저 등록 시 실패해도 로그 등록하기! (REQUIRES_NEW) 활용!
public class LogService {

    private final LogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savaManagerLog(Long userId, Long todoId) {

        Log log = new Log("MangerRequest", userId, todoId);

        logRepository.save(log);
    }
}
