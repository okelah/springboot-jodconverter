package org.jodconverter.office;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by patterncat on 2017-10-22.
 */
public class DefaultConnectionEventListener implements OfficeConnectionEventListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionEventListener.class);

    public void connected(OfficeConnectionEvent event) {
        LOGGER.info("office connected:{}",event);
    }

    public void disconnected(OfficeConnectionEvent event) {
        LOGGER.info("office disconnected:{}",event);
    }
}
