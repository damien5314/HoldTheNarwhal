package com.ddiehl.android.htn.view.markdown;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.di.DaggerTestApplicationComponent;
import com.ddiehl.android.htn.di.SharedModule;
import com.ddiehl.android.htn.di.TestApplicationComponent;
import com.ddiehl.android.htn.di.TestApplicationModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MarkdownParserTest {

    @Inject @Nullable MarkdownParser mMarkdownParser;

    Context getContext() {
        return InstrumentationRegistry.getContext();
    }

    @Before
    public void setUp() {
        TestApplicationComponent component = DaggerTestApplicationComponent.builder()
                .testApplicationModule(new TestApplicationModule(getContext()))
                .sharedModule(new SharedModule())
                .build();
        HoldTheNarwhal.setTestComponent(component);
        component.inject(this);
    }

    @Test
    public void init_markdownParser_isNotNull() {
        assertNotNull(mMarkdownParser);
    }

    @Test
    public void convert_linkWithUnderscores_hasNoInnerFormatting() throws Exception {

    }
}
