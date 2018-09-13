package com.bj58.qf.dubbolight.core.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Filter;

/**
 * Created by Administrator on 2017/12/2.
 */
@Activate(group = Constants.PROVIDER, order = 10000)
public class ProviderAccessLogFilter extends AbstractAccessLogFilter implements Filter {

    public ProviderAccessLogFilter() {
        super(LoggerFactory.getLogger(ProviderAccessLogFilter.class), LoggerFactory.getLogger("providerMonitor"));
    }

}
