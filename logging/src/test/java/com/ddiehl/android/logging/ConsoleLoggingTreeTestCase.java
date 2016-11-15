package com.ddiehl.android.logging;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;


public class ConsoleLoggingTreeTestCase {

    @Test
    public void testConsoleLog() {
        ConsoleLoggingTree tree = new ConsoleLoggingTree(new ConsoleLogger());
        ConsoleLoggingTree spy = Mockito.spy(tree);

        String message = "bar";
        spy.v(message);
        spy.d(message);
        spy.i(message);
        spy.w(message);
        spy.e(message);
        spy.wtf(message);

        Mockito.verify(spy, Mockito.times(6))
                .log(Matchers.anyInt(), Matchers.anyString(), Matchers.eq(message), Matchers.eq((Throwable) null));
    }

    @Test
    public void testLogWarn() {
        ConsoleLoggingTree tree = new ConsoleLoggingTree(new ConsoleLogger());
        tree.w(new RuntimeException());
    }

    @Test(expected = RuntimeException.class)
    public void testLogError() {
        ConsoleLoggingTree tree = new ConsoleLoggingTree(new ConsoleLogger());
        tree.e(new RuntimeException());
    }
}
