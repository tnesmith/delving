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
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Fetch the default language from the theme
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ThemeCookieLocaleResolver extends CookieLocaleResolver {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private ThemeHandler themeHandler;

    @Override
    public String getCookieDomain() {
        log.debug(String.format("Fetching cookie domain %s from theme, cookie name is %s", ThemeFilter.getTheme().getBaseUrl(), getCookieName()));
        return ThemeFilter.getTheme().getBaseUrl();
    }

    @Override
    public String getCookieName() {
        return "org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE";
    }

    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request) {
        final PortalTheme portalTheme = themeHandler.getByRequest(request);
        return new Locale(portalTheme.getDefaultLanguage());
    }
}
