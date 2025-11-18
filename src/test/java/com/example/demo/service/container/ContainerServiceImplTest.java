package com.example.demo.service.container;

import com.example.demo.models.Container;
import com.example.demo.repositories.ContainerRepository;
import com.example.demo.service.container.ContainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Container c = new Container();
        when(repository.save(any())).thenReturn(c);

        Container result = service.create(c);

        assertNotNull(result);
        assertNotNull(c.getLastUpdated());
    }

    @Test
    void testUpdate() {
        Container existing = new Container();
        existing.setId("1");

        Container updated = new Container();
        updated.setFillLevel(90);

        when(repository.findById("1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        Container result = service.update("1", updated);

        assertEquals(90, result.getFillLevel());
    }

    @Test
    void testDelete() {
        doNothing().when(repository).deleteById("1");
        service.delete("1");
        verify(repository, times(1)).deleteById("1");
    }
}