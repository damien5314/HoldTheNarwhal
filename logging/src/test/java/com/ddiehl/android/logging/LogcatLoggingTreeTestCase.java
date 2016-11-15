package com.ddiehl.android.logging;

import org.junit.Test;
import org.mockito.Mockito;


public class LogcatLoggingTreeTestCase {

    @Test
    public void testLogWarn() {
        LogcatLogger logcatLogger = Mockito.mock(LogcatLogger.class);
        LogcatLoggingTree logger = new LogcatLoggingTree(logcatLogger);

        logger.w(new RuntimeException());
    }

    @Test(expected = RuntimeException.class)
    public void testLogError() {
        LogcatLogger logcatLogger = Mockito.mock(LogcatLogger.class);
        LogcatLoggingTree logger = new LogcatLoggingTree(logcatLogger);

        logger.e(new RuntimeException());
    }
}
