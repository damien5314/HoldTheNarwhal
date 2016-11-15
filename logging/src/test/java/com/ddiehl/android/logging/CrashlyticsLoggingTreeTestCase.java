package com.ddiehl.android.logging;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyInt;


public class CrashlyticsLoggingTreeTestCase {

    @Test
    public void testNoLogsBelowInfo() {
        com.ddiehl.android.logging.CrashlyticsLogger crashlyticsLogger = Mockito.mock(com.ddiehl.android.logging.CrashlyticsLogger.class);
        com.ddiehl.android.logging.CrashlyticsLoggingTree tree = new CrashlyticsLoggingTree(crashlyticsLogger);
        com.ddiehl.android.logging.CrashlyticsLoggingTree spy = Mockito.spy(tree);

        String message = "bar";
        spy.v(message);
        spy.d(message);
        spy.i(message);
        spy.w(message);
        spy.e(message);
        spy.wtf(message);

        Mockito.verify(spy, Mockito.times(4))
                .log(anyInt(), Matchers.eq((String) null), Matchers.eq(message), Matchers.eq((Throwable) null));
    }
}
