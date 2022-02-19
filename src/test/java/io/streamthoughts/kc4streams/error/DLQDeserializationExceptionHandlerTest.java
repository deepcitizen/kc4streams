/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kc4streams.error;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static io.streamthoughts.kc4streams.error.DLQExceptionHandlerTestUtils.TEST_CONSUMER_RECORD;
import static io.streamthoughts.kc4streams.error.DLQExceptionHandlerTestUtils.assertProducedRecord;
import static org.apache.kafka.streams.errors.DeserializationExceptionHandler.DeserializationHandlerResponse.CONTINUE;

public class DLQDeserializationExceptionHandlerTest {

  private ProcessorContext mkContext;

  @BeforeEach
  public void setUp() {
    mkContext = Mockito.mock(ProcessorContext.class);
  }

  @BeforeEach
  public void tearDown() {
    DLQRecordCollector.clear();
  }

  @Test
  public void should_send_to_dlq_when_global_producer_is_configured() {

    MockProducer<byte[], byte[]> mkProducer =
        new MockProducer<>(true, new ByteArraySerializer(), new ByteArraySerializer());

    DLQRecordCollector.getOrCreate(
            DLQRecordCollectorConfig.create()
            .withProducer(mkProducer)
            .withAutoCreateTopicEnabled(false)
    );

    final var handler = new DLQDeserializationExceptionHandler();
    handler.configure(Map.of(StreamsConfig.APPLICATION_ID_CONFIG, "test-app"));

    final var exception = new RecordTooLargeException("RecordTooLargeException");
    var response = handler.handle(mkContext, TEST_CONSUMER_RECORD, exception);
    Assertions.assertEquals(CONTINUE, response);
    List<ProducerRecord<byte[], byte[]>> history = mkProducer.history();
    Assertions.assertFalse(history.isEmpty());
    assertProducedRecord(TEST_CONSUMER_RECORD, history.get(0), exception);
  }
}
