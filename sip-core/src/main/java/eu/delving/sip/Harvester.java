/*
 * Copyright 2010 DELVING BV
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


package eu.delving.sip;
/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 27, 2010 9:24:53 PM
 */

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 27, 2010 9:24:53 PM
 */

public class Harvester {
    private Logger log = Logger.getLogger(getClass());
    private Executor executor = Executors.newSingleThreadExecutor();
    private HttpClient httpClient = new HttpClient();
    private XMLInputFactory inputFactory = new WstxInputFactory();
    private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private List<Engine> engines = new CopyOnWriteArrayList<Engine>();

    public interface Harvest {
        String getUrl();

        String getMetadataPrefix();

        String getSpec();

        OutputStream getOutputStream();

        void success();

        void failure(Exception e);
    }

    public void perform(Harvest harvest) {
        Engine engine = new Engine(harvest);
        engines.add(engine);
        executor.execute(engine);
    }

    public List<Harvest> getActive() {
        List<Harvest> active = new ArrayList<Harvest>();
        for (Engine engine : engines) {
            active.add(engine.harvest);
        }
        return active;
    }

    public class Engine implements Runnable {
        private Harvest harvest;
        private XMLEventWriter out;

        public Engine(Harvest harvest) {
            this.harvest = harvest;
            try {
                out = outputFactory.createXMLEventWriter(new OutputStreamWriter(harvest.getOutputStream(), "UTF-8"));
                out.add(eventFactory.createStartDocument());
                out.add(eventFactory.createStartElement("", "", "harvest"));
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            log.info("Harvesting " + harvest.getUrl());
            try {
                HttpMethod method = new GetMethod(String.format(
                        "%s?verb=ListRecords&metadataPrefix=%s&set=%s",
                        harvest.getUrl(),
                        harvest.getMetadataPrefix(),
                        harvest.getSpec()
                ));
                httpClient.executeMethod(method);
                InputStream inputStream = method.getResponseBodyAsStream();
                String resumptionToken = harvestXML(inputStream);
                while (!resumptionToken.isEmpty()) {
                    log.info("Resumption " + resumptionToken);
                    method = new GetMethod(String.format(
                            "%s?verb=ListRecords&resumptionToken=%s",
                            harvest.getUrl(),
                            resumptionToken
                    ));
                    httpClient.executeMethod(method);
                    inputStream = method.getResponseBodyAsStream();
                    resumptionToken = harvestXML(inputStream);
                }
                out.add(eventFactory.createEndElement("", "", "harvest"));
                out.add(eventFactory.createEndDocument());
                out.flush();
                log.info("Finished harvest of " + harvest.getUrl());
                harvest.success();
            }
            catch (Exception e) {
                log.warn("Problem harvesting " + harvest.getUrl(), e);
                log.warn("Exception: " + exceptionToErrorString(e));
                harvest.failure(e);
            }
            finally {
                engines.remove(this);
            }
        }

        private String harvestXML(InputStream inputStream) throws XMLStreamException {
            Source source = new StreamSource(inputStream, "UTF-8");
            XMLEventReader reader = inputFactory.createXMLEventReader(source);
            StringBuilder resumptionToken = null;
            StringBuilder errorString = null;
            boolean withinListRecords = false;
            while (true) {
                XMLEvent event = reader.nextEvent();
                if (withinListRecords) {
                    out.add(event);
                }
                else {
                    switch (event.getEventType()) {
                        case XMLEvent.START_ELEMENT:
                            StartElement startElement = event.asStartElement();
                            if (isErrorElement(startElement.getName())) {
                                errorString = new StringBuilder();
                            }
                            else if (isResumptionToken(startElement.getName())) {
                                resumptionToken = new StringBuilder();
                            }
                            else if (isListRecords(startElement.getName())) {
                                withinListRecords = true;
                            }
                            break;
                        case XMLEvent.CHARACTERS:
                            Characters characters = event.asCharacters();
                            if (!characters.isIgnorableWhiteSpace()) {
                                if (resumptionToken != null) {
                                    resumptionToken.append(characters.getData());
                                }
                                if (errorString != null) {
                                    errorString.append(characters.getData());
                                }
                            }
                            break;
                        case XMLEvent.END_ELEMENT:
                            EndElement endElement = event.asEndElement();
                            if (isErrorElement(endElement.getName())) {
                                throw new XMLStreamException("Error: " + errorString);
                            }
                            else if (isResumptionToken(endElement.getName())) {
                                return "" + resumptionToken;
                            }
                            break;
                    }
                    event = reader.peek();
                    switch (event.getEventType()) {
                        case XMLEvent.END_ELEMENT:
                            EndElement endElement = event.asEndElement();
                            if (isListRecords(endElement.getName())) {
                                withinListRecords = false;
                            }
                            break;
                    }
                }
            }
        }

        private boolean isListRecords(QName name) {
            return "ListRecords".equals(name.getLocalPart());
        }

        private boolean isErrorElement(QName name) {
            return "error".equals(name.getLocalPart());
        }

        private boolean isResumptionToken(QName name) {
            return "resumptionToken".equals(name.getLocalPart());
        }
    }

    static String exceptionToErrorString(Exception exception) {
        StringBuilder out = new StringBuilder();
        out.append(exception.getMessage());
        Throwable cause = exception.getCause();
        while (cause != null) {
            out.append('\n');
            out.append(cause.toString());
            cause = cause.getCause();
        }
        return out.toString();
    }

}
