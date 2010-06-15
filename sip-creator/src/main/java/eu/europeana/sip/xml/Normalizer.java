/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
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

package eu.europeana.sip.xml;

import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.groovy.MappingRunner;
import eu.europeana.sip.model.ConstantFieldModel;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.RecordRoot;
import eu.europeana.sip.model.ToolCodeModel;
import eu.europeana.sip.model.UserNotifier;
import groovy.lang.MissingPropertyException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Take the input and config informationm and produce an output xml file
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Normalizer implements Runnable {
    private FileSet fileSet;
    private AnnotationProcessor annotationProcessor;
    private RecordValidator recordValidator;
    private MetadataParser.Listener parserListener;
    private Listener listener;
    private UserNotifier userNotifier;
    private boolean running = true;

    public interface Listener {
        void invalidInput(MetadataRecord metadataRecord, MissingPropertyException exception);
        void invalidOutput(RecordValidationException exception);
    }

    public Normalizer(
            FileSet fileSet,
            AnnotationProcessor annotationProcessor,
            RecordValidator recordValidator,
            UserNotifier userNotifier,
            MetadataParser.Listener parserListener,
            Listener listener
    ) {
        this.fileSet = fileSet;
        this.annotationProcessor = annotationProcessor;
        this.recordValidator = recordValidator;
        this.userNotifier = userNotifier;
        this.parserListener = parserListener;
        this.listener = listener;
    }

    public void run() {
        try {
            InputStream inputStream = fileSet.getInputStream();
            OutputStream outputStream = fileSet.getOutputStream();
            String mapping = fileSet.getMapping();
            RecordRoot recordRoot = RecordRoot.fromMapping(mapping);
            ConstantFieldModel constantFieldModel = ConstantFieldModel.fromMapping(mapping, annotationProcessor);
            ToolCodeModel toolCodeModel = new ToolCodeModel();
            final Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
            writer.write("<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:europeana=\"http://www.europeana.eu\" xmlns:dcterms=\"http://purl.org/dc/terms/\">\n");
            MappingRunner mappingRunner = new MappingRunner(toolCodeModel.getCode() + mapping, constantFieldModel, new MappingRunner.Listener() {
                @Override
                public void complete(MetadataRecord metadataRecord, Exception exception, String output) {
                    if (exception != null) {
                        if (exception instanceof MissingPropertyException) {
                            MissingPropertyException mpe = (MissingPropertyException)exception;
                            userNotifier.tellUser("Missing property in record "+metadataRecord.getRecordNumber()+": "+mpe.getProperty(), exception);
                            listener.invalidInput(metadataRecord, mpe);
                        }
                        else {
                            userNotifier.tellUser("Problem normalizing record "+metadataRecord.toString(), exception);
                        }
                    }
                    else {
                        try {
                            String validated = recordValidator.validate(metadataRecord, output);
                            writer.write(validated);
                        }
                        catch (RecordValidationException e) {
                            userNotifier.tellUser("Invalid output record", e);
                            listener.invalidOutput(e);
                            running = false;
                        }
                        catch (Exception e) {
                            userNotifier.tellUser("Problem writing output", e);
                            running = false;
                        }
                    }
                }
            });
            MetadataParser parser = new MetadataParser(inputStream, recordRoot, parserListener);
            MetadataRecord record;
            while ((record = parser.nextRecord()) != null && running) {
                if (!mappingRunner.runMapping(record)) {
                    running = false;
                }
            }
            writer.write("</metadata>\n");
            writer.close();
            parser.close();
            if (!running) {
                fileSet.removeOutputFile();
                parserListener.recordsParsed(0, true);
            }
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("XML Problem", e);
        }
        catch (IOException e) {
            throw new RuntimeException("IO Problem", e);
        }
    }

    public void abort() {
        running = false;
    }
}