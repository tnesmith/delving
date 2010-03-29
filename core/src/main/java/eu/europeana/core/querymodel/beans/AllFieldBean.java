/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.core.querymodel.beans;

import eu.europeana.core.querymodel.annotation.Europeana;
import eu.europeana.core.querymodel.annotation.Solr;
import org.apache.solr.client.solrj.beans.Field;

import static eu.europeana.core.querymodel.annotation.ValidationLevel.CopyField;
import static eu.europeana.core.querymodel.annotation.ValidationLevel.EseOptional;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jan 7, 2010 9:17:26 AM
 */

public class AllFieldBean extends FullBean {

    @Field("LOCATION")
    @Europeana(validation = CopyField, facet = false, facetPrefix = "loc", fullDoc = false)
    @Solr(fieldType = "string")
    String[] location;

    @Field("CONTRIBUTOR")
    @Europeana(validation = CopyField, facet = false, facetPrefix = "cont", fullDoc = false)
    @Solr(fieldType = "string")
    String[] contributor;

    @Field("USERTAGS")
    @Europeana(validation = CopyField, facet = false, facetPrefix = "ut", fullDoc = false)
    @Solr(fieldType = "string")
    String[] userTags;

    @Field("SUBJECT")
    @Europeana(validation = CopyField, facet = false, facetPrefix = "sub", fullDoc = false)
    @Solr(fieldType = "string")
    String[] subject;


    @Europeana(validation = EseOptional, fullDoc = false, mappable = true)
    @Solr(namespace = "europeana", name = "unstored", stored = false)
    @Field("europeana_unstored")
    String[] europeanaUnstored;

    // copy fields
    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] text;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] description;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] date;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] format;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] publisher;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] source;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] rights;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] identifier;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] relation;

    // wh copy fields
    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] who;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] when;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] what;

    @Field
    @Europeana(validation = CopyField, fullDoc = false)
    @Solr()
    String[] where;
}
