/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.benchmark.driver.kafka;


import io.openmessaging.benchmark.driver.BenchmarkProducer;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

public class KafkaBenchmarkProducer implements BenchmarkProducer {

    private final Producer<String, byte[]> producer;
    private final String topic;

    public KafkaBenchmarkProducer(Producer<String, byte[]> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public CompletableFuture<Void> sendAsync(Optional<String> key, byte[] payload) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key.orElse(null), payload);

        Header h =
                new RecordHeader(
                        "produce.timestamp",
                        String.valueOf(System.currentTimeMillis()).getBytes(Charset.forName("UTF-8")));
        record.headers().add(h);
        h = new RecordHeader("unique.id",
                RandomStringUtils.randomAlphanumeric(20).getBytes(Charset.forName("UTF-8")));
        record.headers().add(h);
        CompletableFuture<Void> future = new CompletableFuture<>();

        producer.send(
                record,
                (metadata, exception) -> {
                    if (exception != null) {
                        future.completeExceptionally(exception);
                    } else {
                        future.complete(null);
                    }
                });

        return future;
    }

    @Override
    public void close() throws Exception {
        producer.close();
    }
}
