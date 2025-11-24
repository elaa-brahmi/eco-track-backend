package com.example.demo.utils;

import com.example.demo.models.Container;
import com.example.demo.repositories.ContainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class SensorSimulator {

    private final ContainerRepository repo;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 5000)
    public void simulate() {
        List<Container> containers = repo.findAll();

        for (Container c : containers) {
            int noise = new Random().nextInt(6);
            int newFill = Math.min(100, c.getFillLevel() + noise);

            c.setFillLevel(newFill);
            if (newFill >= 90) c.setStatus("ALERT");
            if (newFill == 100) c.setStatus("FULL");

            repo.save(c);

            // BROADCAST LIVE UPDATE
            messagingTemplate.convertAndSend("/topic/containers", c);
        }
    }
}
