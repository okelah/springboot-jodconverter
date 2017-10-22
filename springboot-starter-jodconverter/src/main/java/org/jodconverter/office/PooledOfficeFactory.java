package org.jodconverter.office;

import cn.patterncat.converter.JodConverterProperties;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by patterncat on 2017-10-22.
 */
public class PooledOfficeFactory extends BasePooledObjectFactory<OfficeProcessManager> {

    final AtomicInteger portNumber = new AtomicInteger(2001);

    JodConverterProperties properties;

    public PooledOfficeFactory(JodConverterProperties properties) {
        this.properties = properties;
    }

    @Override
    public OfficeProcessManager create() throws Exception {
        //TODO CONFIG DEFAULT
        OfficeProcessManagerConfig config = new OfficeProcessManagerConfig();
        OfficeUrl officeurl = new OfficeUrl(portNumber.incrementAndGet());
        OfficeProcessManager officeProcessManager = new OfficeProcessManager(officeurl,config);
        // Listen to any connection events to the office instance.
        officeProcessManager.getConnection().addConnectionEventListener(new DefaultConnectionEventListener());
        officeProcessManager.startAndWait();
        return officeProcessManager;
    }

    @Override
    public PooledObject<OfficeProcessManager> wrap(OfficeProcessManager obj) {
        return new DefaultPooledObject<OfficeProcessManager>(obj);
    }

    @Override
    public void destroyObject(PooledObject<OfficeProcessManager> p) throws Exception {
        OfficeProcessManager manager = p.getObject();
        manager.stopAndWait();
    }

    @Override
    public boolean validateObject(PooledObject<OfficeProcessManager> p) {
        return super.validateObject(p);
    }

    @Override
    public void activateObject(PooledObject<OfficeProcessManager> p) throws Exception {
        super.activateObject(p);
    }

    @Override
    public void passivateObject(PooledObject<OfficeProcessManager> p) throws Exception {
        super.passivateObject(p);
    }
}
