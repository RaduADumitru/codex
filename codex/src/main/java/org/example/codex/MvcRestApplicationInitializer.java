package org.example.codex;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MvcRestApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{CodexConfiguration.class};
    }

    @Override
    protected String[] getServletMappings() {
        //possible error: / or codex/?
        return new String[]{"/codex/"};
    }
}
