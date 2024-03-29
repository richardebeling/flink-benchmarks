/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.benchmark;

import org.apache.flink.streaming.api.datastream.DataStream;

import org.apache.flink.api.common.functions.MapFunction;

import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;

import org.apache.flink.formats.avro.typeutils.AvroSerializer;

import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;


@State(Scope.Thread)
public class AvroDeserializationBenchmark extends DeserializationBenchmarkBase {

    private final DataOutputSerializer dataOutput = new DataOutputSerializer(128);
    private final AvroSerializer<NativeTuple> serializer = new AvroSerializer<NativeTuple>(NativeTuple.class);

    private final AvroDeserializationMapper mapper = new AvroDeserializationMapper();

    static class AvroDeserializationMapper implements MapFunction<byte[], NativeTuple> {
        private final AvroSerializer<NativeTuple> serializer = new AvroSerializer<NativeTuple>(NativeTuple.class);
        private final DataInputDeserializer dataInput = new DataInputDeserializer();

        @Override
        public NativeTuple map(byte[] bytes) throws Exception {
            dataInput.setBuffer(bytes);
            return serializer.deserialize(dataInput);
        }
    }

    @Override
    public byte[] serializeNativeTuple(NativeTuple tup) throws Exception {
        dataOutput.clear();
        serializer.serialize(tup, dataOutput);
        return dataOutput.getCopyOfBuffer();
    }

    @Override
    public DataStream<NativeTuple> addDeserializationOperations(DataStream<byte[]> stream) {
        return stream.map(mapper);
    }
}
