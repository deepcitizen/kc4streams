/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kc4streams.rocksdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.InfoLogLevel;

import java.util.HashMap;
import java.util.Map;

public class StreamsRocksDBConfigSetterConfigTest {

    @Test
    public void should_create_config_given_valid_props() {

        Map<String, Object> props = new HashMap<>();

        props.put(RocksDBConfig.ROCKSDB_STATS_DUMP_PERIOD_SEC_CONFIG, 100);
        props.put(RocksDBConfig.ROCKSDB_STATS_ENABLE_CONFIG, true);
        props.put(RocksDBConfig.ROCKSDB_MAX_LOG_FILE_SIZE_CONFIG, 200);
        props.put(RocksDBConfig.ROCKSDB_LOG_DIR_CONFIG, "/log/dir");
        props.put(RocksDBConfig.ROCKSDB_LOG_LEVEL_CONFIG, InfoLogLevel.DEBUG_LEVEL.name());
        props.put(RocksDBConfig.ROCKSDB_MAX_WRITE_BUFFER_NUMBER_CONFIG, 4);
        props.put(RocksDBConfig.ROCKSDB_WRITE_BUFFER_SIZE_CONFIG, 4);
        props.put(RocksDBConfig.ROCKSDB_MEMORY_MANAGED_CONFIG, true);
        props.put(RocksDBConfig.ROCKSDB_MEMORY_WRITE_BUFFER_RATIO_CONFIG, 0.1);
        props.put(RocksDBConfig.ROCKSDB_MEMORY_HIGH_PRIO_POOL_RATIO_CONFIG, 0.2);
        props.put(RocksDBConfig.ROCKSDB_MEMORY_STRICT_CAPACITY_LIMIT_CONFIG, true);
        props.put(RocksDBConfig.ROCKSDB_BLOCK_CACHE_SIZE_CONFIG, 32);
        props.put(RocksDBConfig.ROCKSDB_COMPACTION_STYLE_CONFIG, CompactionStyle.LEVEL.name());
        props.put(RocksDBConfig.ROCKSDB_COMPRESSION_TYPE_CONFIG, CompressionType.LZ4_COMPRESSION.name());
        props.put(RocksDBConfig.ROCKSDB_FILES_OPEN_CONFIG, 1);

        RocksDBConfig config = new RocksDBConfig(props);

        Assertions.assertEquals(100, config.getDumpPeriodSec());
        Assertions.assertTrue(config.isStatisticsEnable());
        Assertions.assertEquals(200, config.getMaxLogFileSize().get());
        Assertions.assertEquals("/log/dir", config.getLogDir());
        Assertions.assertEquals(4, config.getMaxWriteBufferNumber().get());
        Assertions.assertEquals(4, config.getWriteBufferSize().get());
        Assertions.assertTrue(config.isMemoryManaged());
        Assertions.assertEquals(0.1, config.getMemoryWriteBufferRatio());
        Assertions.assertEquals(0.2, config.getMemoryHighPrioPoolRatio());
        Assertions.assertEquals(true, config.getMemoryStrictCapacityLimit());
        Assertions.assertEquals(32, config.getBlockCacheSize().get());
        Assertions.assertEquals(CompactionStyle.LEVEL, config.getCompactionStyle().get());
        Assertions.assertEquals(CompressionType.LZ4_COMPRESSION, config.getCompressionType().get());
        Assertions.assertEquals(1, config.getMaxOpenFile());
    }
}