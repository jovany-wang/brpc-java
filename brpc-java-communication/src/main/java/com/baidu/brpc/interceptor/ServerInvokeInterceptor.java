/*
 * Copyright (c) 2019 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.brpc.interceptor;

import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ServerInvokeInterceptor extends AbstractInterceptor {

    @Override
    public void aroundProcess(Request request, Response response, InterceptorChain chain) throws RpcException {
        try {
            Method methodToBeInvoked = request.getTargetMethod();
            if (methodToBeInvoked.getReturnType().equals(CompletableFuture.class)) {
                // 异步 server

            } else {
                response.setResult(methodToBeInvoked.invoke(request.getTarget(), request.getArgs()));
            }
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException == null) {
                targetException = ex;
            }
            String errorMsg = String.format("invoke method failed, msg=%s", targetException.getMessage());
            log.warn(errorMsg, targetException);
            response.setException(targetException);
        } catch (Throwable ex) {
            String errorMsg = String.format("invoke method failed, msg=%s", ex.getMessage());
            log.warn(errorMsg, ex);
            response.setException(ex);
        }
    }
}
