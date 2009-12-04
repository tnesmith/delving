/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

package eu.europeana.query;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class EuropeanaQueryException extends Exception {
    private static final long serialVersionUID = 102062830667043265L;
    private QueryProblem queryProblem;

    public EuropeanaQueryException(String message) {
        super(message);
        queryProblem = QueryProblem.get(message);
    }

    public EuropeanaQueryException(String message, Throwable e) {
        super(message, e);
        queryProblem = QueryProblem.get(e.toString());
        if (queryProblem == QueryProblem.UNKNOWN) {
            queryProblem = QueryProblem.get(message);
        }
    }

    public QueryProblem getFetchProblem() {
        return queryProblem;
    }
}