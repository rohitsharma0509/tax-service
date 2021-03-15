package com.scb.rider.tax.service.impl;

import com.scb.rider.tax.model.document.DatabaseSequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class SequenceGeneratorServiceTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private SequenceGeneratorService service;

    @Test
    void generateSequenceTest() {
        DatabaseSequence databaseSequence = new DatabaseSequence("abc_sequence", 10000);
        when(mongoOperations.findAndModify(any(), any(),
                any(), (Class<Object>) any())).thenReturn(databaseSequence);
        String sequence = service.generateSequence("abc_sequence");
        assertNotNull(sequence);
    }

    @Test
    void generateSequenceDatabaseIsNullTest() {
        DatabaseSequence databaseSequence = new DatabaseSequence("abc_sequence", 10000);
        when(mongoOperations.findAndModify(any(), any(),
                any(), (Class<Object>) any())).thenReturn(null);
        when(mongoOperations.save(any())).thenReturn(databaseSequence);
        String sequence = service.generateSequence("abc_sequence");
        assertNotNull(sequence);
    }

}
