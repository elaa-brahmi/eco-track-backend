package com.example.demo.service.container;

import com.example.demo.dto.ContainerDto;
import com.example.demo.models.Container;
import com.example.demo.repositories.ContainerRepository;
import com.example.demo.service.container.ContainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ContainerServiceImplTest {

    @Mock
    private ContainerRepository repository;

    @InjectMocks
    private ContainerServiceImpl service;

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(new Container()));

        List<Container> result = service.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testFindById() {
        //given
        Container c = new Container();
        c.setId("1");

        when(repository.findById("1")).thenReturn(Optional.of(c));
        //when
        Container result = service.findById("1");
        //then
        assertNotNull(result);
        assertEquals("1", result.getId());
    }

    @Test
    void testCreate() {
        ContainerDto requestDto = new ContainerDto();
        requestDto.setType("paper");
        requestDto.setStatus("overflowing");
        requestDto.setLocation(new double[]{30.251, 145.252});

        Container savedContainer = new Container();
        savedContainer.setId("edfrefdrefdrzfdr");
        savedContainer.setType("paper");
        savedContainer.setStatus("overflowing");
        savedContainer.setLocation(new double[]{30.251, 145.252});
        savedContainer.setLastUpdated(Instant.now());

        when(repository.save(any(Container.class))).thenReturn(savedContainer);
        //when
        Container result = service.create(requestDto);

        // Then - Verify results
        assertNotNull(result);
        assertNotNull(result.getLastUpdated());
        assertEquals("paper", result.getType());
        assertEquals("overflowing", result.getStatus());
        assertArrayEquals(new double[]{30.251, 145.252}, result.getLocation(), 0.0001);

        // Verify that save was called exactly once
        verify(repository, times(1)).save(any(Container.class));
    }

    @Test
    void testDelete() {
        doNothing().when(repository).deleteById("1");
        service.delete("1");
        verify(repository, times(1)).deleteById("1");
    }
}