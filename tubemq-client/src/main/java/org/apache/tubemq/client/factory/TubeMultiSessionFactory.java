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

package org.apache.tubemq.client.factory;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.tubemq.client.config.ConsumerConfig;
import org.apache.tubemq.client.config.TubeClientConfig;
import org.apache.tubemq.client.config.TubeClientConfigUtils;
import org.apache.tubemq.client.consumer.PullMessageConsumer;
import org.apache.tubemq.client.consumer.PushMessageConsumer;
import org.apache.tubemq.client.exception.TubeClientException;
import org.apache.tubemq.client.producer.MessageProducer;
import org.apache.tubemq.corebase.Shutdownable;
import org.apache.tubemq.corerpc.RpcConfig;
import org.apache.tubemq.corerpc.netty.NettyClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TubeMultiSessionFactory implements MessageSessionFactory {

    private static final Logger logger =
            LoggerFactory.getLogger(TubeMultiSessionFactory.class);
    private final NettyClientFactory clientFactory = new NettyClientFactory();
    private final TubeBaseSessionFactory baseSessionFactory;
    private final AtomicBoolean isShutDown = new AtomicBoolean(false);

    public TubeMultiSessionFactory(final TubeClientConfig tubeClientConfig) throws TubeClientException {
        RpcConfig config = TubeClientConfigUtils.getRpcConfigByClientConfig(tubeClientConfig, false);
        clientFactory.configure(config);
        baseSessionFactory = new TubeBaseSessionFactory(clientFactory, tubeClientConfig);
    }

    @Override
    public void shutdown() throws TubeClientException {
        if (isShutDown.compareAndSet(false, true)) {
            baseSessionFactory.shutdown();
            clientFactory.shutdown();
        }
    }

    @Override
    public <T extends Shutdownable> void removeClient(final T client) {
        this.baseSessionFactory.removeClient(client);
    }

    @Override
    public MessageProducer createProducer() throws TubeClientException {
        return this.baseSessionFactory.createProducer();
    }

    @Override
    public PushMessageConsumer createPushConsumer(final ConsumerConfig consumerConfig)
            throws TubeClientException {
        return this.baseSessionFactory.createPushConsumer(consumerConfig);
    }

    @Override
    public PullMessageConsumer createPullConsumer(ConsumerConfig consumerConfig)
            throws TubeClientException {
        return this.baseSessionFactory.createPullConsumer(consumerConfig);
    }

}
