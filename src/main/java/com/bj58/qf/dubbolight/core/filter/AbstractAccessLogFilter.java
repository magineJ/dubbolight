package com.bj58.qf.dubbolight.core.filter;


import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSON;
import com.bj58.qf.dubbolight.core.enums.LogFilterLevel;
import org.joda.time.DateTime;

import java.util.ResourceBundle;

/**
 * Created by Administrator on 2017/12/2.
 */
public class AbstractAccessLogFilter implements Filter {
    private final Logger logger;
    private final Logger accesslogger;
    private static Integer logLevel;

    private static ResourceBundle rb;

    static {
        try {
            rb = ResourceBundle.getBundle("dubbo");
            if (rb == null) {
                logLevel = LogFilterLevel.ARGUMENTS.level;
            } else {
                if (rb.getString("dubbo.logLevel") == null) {
                    logLevel = LogFilterLevel.ARGUMENTS.level;
                } else {
                    logLevel = Integer.valueOf(rb.getString("dubbo.logLevel"));
                }
            }
        } catch (Exception e) {
            logLevel = LogFilterLevel.ARGUMENTS.level;
        }
    }

    public AbstractAccessLogFilter(Logger logger, Logger accesslogger) {
        this.logger = logger;
        this.accesslogger = accesslogger;
    }

    /**
     * do invoke filter.
     * <p>
     * <code>
     * // before filter
     * Result result = invoker.invoke(invocation);
     * // after filter
     * return result;
     * </code>
     *
     * @param invoker service
     * @param inv     invocation.
     * @return invoke result.
     * @throws RpcException
     * @see Invoker#invoke(Invocation)
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation inv) {
        Result result = null;
        Throwable t = null;
        boolean isExceptionNeedsToBeThrown = false;

        try {
            InvocationLinks links = new InvocationLinks(inv);
            long start = System.currentTimeMillis();

            try {
                result = invoker.invoke(inv);
            } catch (RuntimeException e) {
                t = e;
                isExceptionNeedsToBeThrown = true;
            }

            if (result != null && result.hasException()) {
                t = result.getException();
            }

            long end = System.currentTimeMillis();

            StringBuilder fqcn = new StringBuilder(getRecordHeader(links, start, end));
            fqcn.append(appendRecordAccess(logLevel, invoker, inv, end - start, t));
            fqcn.append(appendRecordArgs(logLevel, inv));
            fqcn.append(appendRecordReturnValue(logLevel, result));

            if (fqcn.length() > 0) {
                accesslogger.info(fqcn.toString());
            }

        } catch (Throwable th) {
            logger.warn("Exception in AccessLogFilter of service(" + invoker + " -> " + inv + ")", th);
        }

        if (isExceptionNeedsToBeThrown) {
            throw (RuntimeException) t;
        }

        return result;
    }

    private String getRecordHeader(InvocationLinks links, long start, long end) {
        StringBuilder fqcn = new StringBuilder();
        DateTime startTime = new DateTime(start);
        String startTimeString = startTime.toString("yyyy-MM-dd HH:mm:ss.SSS");
        DateTime endTime = new DateTime(end);
        String endTimeString = endTime.toString("yyyy-MM-dd HH:mm:ss.SSS");
        String c_host = links.getLocalHost();
        int c_port = links.getLocalPort();
        String p_host = links.getRemoteHost();
        int p_port = links.getRemotePort();
        String c_app = links.getApplication();
        String p_app = "nil";

        if (!links.isConsumerSide()) {
            c_host = links.getRemoteHost();
            c_port = links.getRemotePort();
            p_host = links.getLocalHost();
            p_port = links.getLocalPort();
        }

        if (links.getTraceId() != null) {
            fqcn.append("traceId: ").append(links.getTraceId()).append("  ");
        }

        fqcn.append("consumer[").append(c_app).append(",").append(c_host).append(':').append(c_port).append(",").append(startTimeString).append("]").append(" -> ").append("provider[").append(p_app).append(",").append(p_host).append(':').append(p_port).append(",").append(endTimeString).append("]").append(" - ");
        return fqcn.toString();

    }

    private String appendRecordAccess(int logLevel, Invoker<?> invoker, Invocation invocation, long elapsed, Throwable t) {
        StringBuilder fqcn = new StringBuilder();
        if (logLevel < LogFilterLevel.ACCESS.level) {
            return fqcn.toString();
        } else {
            String serviceName = invoker.getInterface().getName();
            String version = invoker.getUrl().getParameter("version");
            String group = invoker.getUrl().getParameter("group");
            if (null != group && group.length() > 0) {
                fqcn.append(group).append("/");
            }

            fqcn.append(serviceName);
            if (null != version && version.length() > 0) {
                fqcn.append(":").append(version);
            }

            fqcn.append(" ");
            fqcn.append(invocation.getMethodName());
            fqcn.append("(");
            Class<?>[] types = invocation.getParameterTypes();
            if (types != null && types.length > 0) {
                boolean first = true;

                int parameterTypesLength = types.length;

                for (int i = 0; i < parameterTypesLength; ++i) {
                    Class<?> type = types[i];
                    if (first) {
                        first = false;
                    } else {
                        fqcn.append(",");
                    }

                    fqcn.append(type.getName());
                }
            }

            fqcn.append(") ");
            fqcn.append("elapse:").append(elapsed).append("ms ");
            if (t != null) {
                fqcn.append(" FAILED(").append(t.getMessage()).append(") ");
            } else {
                fqcn.append(" SUCCESS ");
            }

            return fqcn.toString();
        }
    }

    private String appendRecordArgs(int logLevel, Invocation invocation) {
        StringBuilder fqcn = new StringBuilder();
        if (logLevel < LogFilterLevel.ARGUMENTS.level) {
            return fqcn.toString();
        } else {
            Object[] args = invocation.getArguments();
            if (args != null && args.length > 0) {
                fqcn.append(JSON.toJSONString(args));
            }

            return fqcn.toString();
        }
    }

    private String appendRecordReturnValue(int logLevel, Result result) {
        StringBuilder fqcn = new StringBuilder();
        if (logLevel < LogFilterLevel.RETURNS.level) {
            return fqcn.toString();
        } else if (result.hasException()) {
            fqcn.append("FAILED(").append(result.getException().getMessage()).append(") ");
            return fqcn.toString();
        } else {
            fqcn.append("\n Return value(").append(JSON.toJSONString(result.getValue())).append(") ");
            return fqcn.toString();
        }
    }

    protected class InvocationLinks {
        private String application;
        private String localHost;
        private int localPort;
        private String remoteHost;
        private int remotePort;
        private boolean consumerSide;
        private String traceId;

        private InvocationLinks(Invocation invocation) {
            RpcContext context = RpcContext.getContext();
            this.consumerSide = context.isConsumerSide();
            this.localHost = context.getLocalHost();
            this.localPort = context.getLocalPort();
            this.remoteHost = context.getRemoteHost();
            this.remotePort = context.getRemotePort();

            if (invocation.getAttachment("traceId") != null) {
                this.traceId = invocation.getAttachment("traceId");
            }

            application = invocation.getAttachment("application");
            if (StringUtils.isBlank(application)) {
                logger.debug("the consumer:" + isConsumerSide() + " end-point cannot get application attachment");
            }
        }

        public String getApplication() {
            return this.application;
        }

        public String getLocalHost() {
            return this.localHost;
        }

        public int getLocalPort() {
            return this.localPort;
        }

        public String getRemoteHost() {
            return this.remoteHost;
        }

        public int getRemotePort() {
            return this.remotePort;
        }

        public boolean isConsumerSide() {
            return this.consumerSide;
        }

        public String getTraceId() {
            return traceId;
        }
    }
}
