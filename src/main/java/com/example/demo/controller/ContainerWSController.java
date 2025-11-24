package com.example.demo.controller;

import com.example.demo.models.Container;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
//Frontend sends message → /app/updateContainer

//Backend broadcasts to → /topic/containers

//Manual updates from frontend or employee app
@RestController
@RequiredArgsConstructor
public class ContainerWSController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/updateContainer")
    public void manuallyUpdate(Container container) {
        messagingTemplate.convertAndSend("/topic/containers", container);
    }
}
