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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DLQExceptionHandlerTestUtils {

  public static final String TEST_TOPIC = "test-topic";
  public static final String TEST_APP = "test-app";
  public static final String TEST_RECORD_KEY = "test-key";
  public static final String TEST_RECORD_VALUE = "test-value";

  public static final Headers TEST_HEADERS =
      new RecordHeaders()
          .add("test-header-key", "test-header-value".getBytes(StandardCharsets.UTF_8));

  static final ProducerRecord<byte[], byte[]> TEST_PRODUCER_RECORD =
      new ProducerRecord<>(
          DLQExceptionHandlerTestUtils.TEST_TOPIC,
          0,
          TEST_RECORD_KEY.getBytes(StandardCharsets.UTF_8),
          TEST_RECORD_VALUE.getBytes(StandardCharsets.UTF_8),
          TEST_HEADERS);

  static final ConsumerRecord<byte[], byte[]> TEST_CONSUMER_RECORD =
      new ConsumerRecord<>(
          DLQExceptionHandlerTestUtils.TEST_TOPIC,
          0,
          1235L,
          0L,
          TimestampType.CREATE_TIME,
          0L,
          ConsumerRecord.NULL_SIZE,
          ConsumerRecord.NULL_SIZE,
          TEST_RECORD_KEY.getBytes(StandardCharsets.UTF_8),
          TEST_RECORD_VALUE.getBytes(StandardCharsets.UTF_8),
          TEST_HEADERS);

  public static void assertProducedRecord(
      final ProducerRecord<byte[], byte[]> source,
      final ProducerRecord<byte[], byte[]> record,
      final Exception e) {

    Map<String, String> headers = assertCommonsHeadersAndGet(record, e);

    assertEquals(source.topic(), headers.get(ExceptionHeaders.ERROR_RECORD_TOPIC));
    assertEquals(String.valueOf(source.partition()), headers.get(ExceptionHeaders.ERROR_RECORD_PARTITION));

    Assertions.assertEquals(ExceptionType.PRODUCTION.name(), headers.get(ExceptionHeaders.ERROR_TYPE));
  }

  public static void assertProducedRecord(
      final ConsumerRecord<byte[], byte[]> source,
      final ProducerRecord<byte[], byte[]> record,
      final Exception e) {

    Map<String, String> headers = assertCommonsHeadersAndGet(record, e);

    assertEquals(source.topic(), headers.get(ExceptionHeaders.ERROR_RECORD_TOPIC));
    assertEquals(String.valueOf(source.partition()), headers.get(ExceptionHeaders.ERROR_RECORD_PARTITION));
    assertEquals(String.valueOf(source.offset()), headers.get(ExceptionHeaders.ERROR_RECORD_OFFSET));

    Assertions.assertEquals(ExceptionType.DESERIALIZATION.name(), headers.get(ExceptionHeaders.ERROR_TYPE));
  }

  private static Map<String, String> assertCommonsHeadersAndGet(
      final ProducerRecord<byte[], byte[]> record, final Exception e) {

    assertEquals(TEST_TOPIC + DefaultDLQTopicNameExtractor.DEFAULT_SUFFIX, record.topic());

    final Map<String, String> headers =
        Stream.of(record.headers().toArray())
            .collect(Collectors.toMap(Header::key, h -> new String(h.value())));

    assertEquals(TEST_APP, headers.get(ExceptionHeaders.ERROR_APPLICATION_ID));
    assertEquals(e.getMessage(), headers.get(ExceptionHeaders.ERROR_EXCEPTION_MESSAGE));
    assertEquals(e.getClass().getName(), headers.get(ExceptionHeaders.ERROR_EXCEPTION_CLASS_NAME));
    assertEquals(ExceptionHeaders.getStacktrace(e), headers.get(ExceptionHeaders.ERROR_EXCEPTION_STACKTRACE));
    assertNotNull(headers.get(ExceptionHeaders.ERROR_TIMESTAMP));
    assertEquals("test-header-value", headers.get("test-header-key"));
    return headers;
  }
}
