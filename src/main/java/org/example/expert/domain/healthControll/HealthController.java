package org.example.expert.domain.healthControll;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    // EC2 연결 확인을 위한 health API
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
