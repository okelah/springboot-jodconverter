package org.jodconverter.office;

import cn.patterncat.converter.JodConverterProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jodconverter.task.OfficeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by patterncat on 2017-10-22.
 */
public class OfficeManagerAdapter implements OfficeManager, TemporaryFileMaker{

    private static final Logger LOGGER = LoggerFactory.getLogger(OfficeManagerAdapter.class);

    OfficePool officePool;

    JodConverterProperties properties;

    protected final SuspendableThreadPoolExecutor taskExecutor;

    public OfficeManagerAdapter(OfficePool officePool, JodConverterProperties properties) {
        this.officePool = officePool;
        this.properties = properties;
        this.taskExecutor = new SuspendableThreadPoolExecutor(new NamedThreadFactory("task-queue"));
    }

    File tempDir;

    private final AtomicLong tempFileCounter = new AtomicLong(0);

    //注意这里不需要加volatile
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * todo 考虑把超时参数抽取出来到方法级别
     * todo 可以用hystrix替代默认的超时方式
     * @param task
     * @throws OfficeException
     */
    public void execute(final OfficeTask task) throws OfficeException {
        if(!isRunning()){
            throw new IllegalStateException("system is shutting down");
        }
        OfficeProcessManager manager = null;
        try {
            manager = officePool.borrowObject(properties.getTaskQueueTimeout());
            final OfficeConnection connection = manager.getConnection();
            Future<?> currentFuture = taskExecutor.submit(
                    new Callable<Void>() {
                        public Void call() throws Exception {
                            task.execute(connection);
//                            LOGGER.info("finish task execution");
                            return null;
                        }
                    });
            currentFuture.get(properties.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
        } catch (NoSuchElementException e) {
            throw new OfficeException("no office available",e);
        } catch (TimeoutException e){
            throw new OfficeException("task execute timeout",e);
        } catch (Exception e){
            throw new OfficeException(e.getMessage(),e);
        } finally {
            if(manager != null){
                officePool.returnObject(manager);
            }
        }
    }

    public boolean isRunning() {
        return running.get(); //原来的体系是在execute之前调用这个方法先判断一下
    }

    public void start() throws OfficeException {
        // Create the temporary dir is the pool has successfully started
        String workingDir = StringUtils.isBlank(properties.getWorkingDir()) ? System.getProperty("java.io.tmpdir") : properties.getWorkingDir();
        tempDir = makeTempDir(new File(workingDir));
        taskExecutor.setAvailable(true);
    }

    public void stop() throws OfficeException {
        running.set(false);
        taskExecutor.setAvailable(false);
        deleteTempDir();
    }

    public File makeTemporaryFile(String extension) {
        return new File(tempDir, "tempfile_" + tempFileCounter.getAndIncrement() + "." + extension);
    }

    private void deleteTempDir() {
        if (tempDir != null) {
            LOGGER.info("Deleting temporary directory '{}'", tempDir);
            try {
                FileUtils.deleteDirectory(tempDir);
            } catch (IOException ioEx) { // NOSONAR
                LOGGER.error("Could not temporary profileDir: {}", ioEx.getMessage());
            }
        }
    }

    static File makeTempDir(final File workingDir) {
        final File tempDir = new File(workingDir, "jodconverter_" + UUID.randomUUID().toString());
        tempDir.mkdir();
        if (!tempDir.isDirectory()) {
            throw new IllegalStateException(String.format("Cannot create temp directory: %s", tempDir));
        }
        return tempDir;
    }
}
