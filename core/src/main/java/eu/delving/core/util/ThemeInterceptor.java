/*
 * Copyright 2011 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.delving.core.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Put a theme into thread local for subsequent use during handling of the request
 *
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class ThemeInterceptor extends HandlerInterceptorAdapter {

    private static ThreadLocal<PortalTheme> themeThreadLocal = new ThreadLocal<PortalTheme>();

    @Autowired
    private ThemeHandler themeHandler;

    public static PortalTheme getTheme() {
        if (themeThreadLocal.get() == null) {
            Logger.getLogger(ThemeInterceptor.class).error("This interceptor must be installed!");
            return null;
        }
        return themeThreadLocal.get();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final PortalTheme portalTheme = themeHandler.getByRequest(request);
        themeThreadLocal.set(portalTheme);
        return true;
    }
}