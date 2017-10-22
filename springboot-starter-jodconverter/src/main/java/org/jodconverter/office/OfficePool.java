package org.jodconverter.office;

import lombok.experimental.Delegate;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by patterncat on 2017-10-22.
 */
public class OfficePool {

    @Delegate
    GenericObjectPool<OfficeProcessManager> innerPool;

    public OfficePool(PooledObjectFactory<OfficeProcessManager> factory, GenericObjectPoolConfig config) {
        this.innerPool = new GenericObjectPool<OfficeProcessManager>(factory, config);
    }
}
