/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tubemq.client.producer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tubemq.client.exception.TubeClientException;
import org.apache.tubemq.corebase.Message;
import org.apache.tubemq.corebase.cluster.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RoundRobinPartitionRouter implements PartitionRouter {

    private static final Logger logger =
            LoggerFactory.getLogger(RoundRobinPartitionRouter.class);
    private final AtomicInteger steppedCounter = new AtomicInteger(0);
    private final ConcurrentHashMap<String/* topic */, AtomicInteger> partitionRouterMap =
            new ConcurrentHashMap<String, AtomicInteger>();

    @Override
    public Partition getPartition(final Message message, final List<Partition> partitions) throws TubeClientException {
        if (partitions == null || partitions.isEmpty()) {
            throw new TubeClientException(new StringBuilder(512)
                    .append("No available partition for topic: ")
                    .append(message.getTopic()).toString());
        }
        AtomicInteger currRouterCount = partitionRouterMap.get(message.getTopic());
        if (null == currRouterCount) {
            AtomicInteger newCounter = new AtomicInteger(0);
            currRouterCount = partitionRouterMap.putIfAbsent(message.getTopic(), newCounter);
            if (null == currRouterCount) {
                currRouterCount = newCounter;
            }
        }
        Partition roundPartition = null;
        int partSize = partitions.size();
        for (int i = 0; i < partSize; i++) {
            roundPartition =
                    partitions.get((currRouterCount.incrementAndGet() & Integer.MAX_VALUE) % partSize);
            if (roundPartition != null) {
                if (roundPartition.getDelayTimeStamp() < System.currentTimeMillis()) {
                    return roundPartition;
                }
            }
        }
        return partitions.get((steppedCounter.incrementAndGet() & Integer.MAX_VALUE) % partSize);
    }

}
