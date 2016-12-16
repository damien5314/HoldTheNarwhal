package com.ddiehl.android.htn.di;

import com.ddiehl.android.htn.view.markdown.MarkdownParserTest;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                TestApplicationModule.class,
                SharedModule.class,
        }
)
public interface TestApplicationComponent extends ApplicationComponent {

    void inject(MarkdownParserTest test);
}
