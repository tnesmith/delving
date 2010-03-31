/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.web.controller;

import eu.europeana.core.util.web.CarouselItemSampler;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import eu.europeana.core.util.web.ProposedSearchTermSampler;
import eu.europeana.definitions.domain.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Where people arrive.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 */

@Controller
public class IndexPageController {

    @Autowired
    private CarouselItemSampler carouselItemSampler;

    @Autowired
    private ProposedSearchTermSampler proposedSearchTermSampler;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @RequestMapping("/index.html")
    public ModelAndView indexHandler (HttpServletRequest request) throws Exception {
        final ModelAndView page = ControllerUtil.createModelAndViewPage("index_orig");
        Language language = ControllerUtil.getLocale(request);
        page.addObject("proposedSearchTerms", proposedSearchTermSampler.pickRandomItems(language));
        page.addObject("carouselItems", carouselItemSampler.pickShuffledRandomItems());
        clickStreamLogger.logUserAction(request, ClickStreamLogger.UserAction.INDEXPAGE, page);
        return page;
    }



}