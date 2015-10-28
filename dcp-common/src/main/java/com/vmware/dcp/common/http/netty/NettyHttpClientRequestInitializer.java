/*
 * Copyright (c) 2015 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, without warranties or
 * conditions of any kind, EITHER EXPRESS OR IMPLIED.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.vmware.dcp.common.http.netty;

import javax.net.ssl.SSLEngine;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.ssl.SslHandler;

import com.vmware.dcp.common.Operation.SocketContext;

class NettyHttpClientRequestInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyChannelPool pool;

    public NettyHttpClientRequestInitializer(NettyChannelPool nettyChannelPool) {
        this.pool = nettyChannelPool;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        ch.config().setAllocator(NettyChannelContext.ALLOCATOR);
        ch.config().setSendBufferSize(NettyChannelContext.BUFFER_SIZE);
        ch.config().setReceiveBufferSize(NettyChannelContext.BUFFER_SIZE);
        if (this.pool.getSSLContext() != null) {
            SSLEngine engine = this.pool.getSSLContext().createSSLEngine();
            engine.setUseClientMode(true);
            p.addLast("ssl", new SslHandler(engine));
        }
        p.addLast("encoder", new HttpRequestEncoder());
        p.addLast("decoder", new HttpResponseDecoder());
        p.addLast("aggregator", new HttpObjectAggregator(SocketContext.MAX_REQUEST_SIZE));
        p.addLast(new NettyHttpServerResponseHandler(this.pool));
    }
}