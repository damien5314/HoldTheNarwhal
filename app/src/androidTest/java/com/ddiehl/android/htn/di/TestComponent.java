package com.ddiehl.android.htn.di;

import com.ddiehl.android.htn.view.markdown.MarkdownParserTest;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                SharedModule.class
        }
)
public interface TestComponent {

    void inject(MarkdownParserTest test);
}
