package com.bj58.qf.dubbolight.core.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Filter;

/**
 * Created by Administrator on 2017/12/2.
 */
@Activate(group = Constants.CONSUMER, order = 10000)
public class ConsumerAccessLogFilter extends AbstractAccessLogFilter implements Filter {
    public ConsumerAccessLogFilter() {
        super(LoggerFactory.getLogger(ProviderAccessLogFilter.class), LoggerFactory.getLogger("consumerMonitor"));
    }

}
