package eu.delving.services.core;

import com.ctc.wstx.stax.WstxInputFactory;
import eu.delving.metadata.FieldDefinition;
import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.Path;
import eu.delving.metadata.Tag;
import eu.delving.services.exceptions.HarvindexingException;
import eu.delving.sip.AccessKey;
import eu.delving.sip.DataSetState;
import eu.europeana.core.querymodel.query.DocType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 27, 2010 9:24:53 PM
 */

public class Harvindexer {
    private SolrServer solrServer;
    private XMLInputFactory inputFactory = new WstxInputFactory();
    private Executor executor = Executors.newSingleThreadExecutor();
    private Logger log = Logger.getLogger(getClass());
    private int chunkSize = 1000;
    private HttpClient httpClient;
    private List<Processor> processors = new CopyOnWriteArrayList<Processor>();


    @Value("#{launchProperties['services.harvindexing.prefix']}")
    private String metadataPrefix;

    @Value("#{launchProperties['services.url']}")
    private String servicesUrl;

    @Autowired
    private AccessKey accessKey;

    @Autowired
    private MetadataModel metadataModel;

    @Autowired
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Autowired
    public void setSolrServer(@Qualifier("solrUpdateServer") SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void commenceImport(MetaRepo.DataSet dataSet) {
        for (Processor processor : processors) {
            if (processor.dataSet.equals(dataSet)) {
                return;
            }
        }
        Processor processor = new Processor(dataSet);
        processors.add(processor);
        processor.start();
    }

    public List<MetaRepo.DataSet> getActiveImports() {
        List<MetaRepo.DataSet> active = new ArrayList<MetaRepo.DataSet>();
        for (Processor processor : processors) {
            active.add(processor.getDataSet());
        }
        return active;
    }

    public void commitSolr() throws IOException, SolrServerException {
        solrServer.commit();
    }

    public class Processor implements Runnable {
        private Thread thread;
        private MetaRepo.DataSet dataSet;

        public Processor(MetaRepo.DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public MetaRepo.DataSet getDataSet() {
            return dataSet;
        }

        public void start() {
            if (this.thread == null) {
                this.thread = new Thread(this);
                thread.setName(String.format("Harvindexer Processor for %s", dataSet.getSpec()));
                thread.start();
            }
            else {
                log.warn(String.format("Processor already started for %s", dataSet.getSpec()));
            }
        }

        public void stop() {
            if (thread != null) {
                Thread threadToJoin = thread;
                thread = null;
                try {
                    threadToJoin.join();
                }
                catch (InterruptedException e) {
                    log.error("Interrupted!", e);
                }
            }
        }

        @Override
        public void run() {
            log.info("Importing " + dataSet);
            int retries = 0;
            final int nrOfRetries = 3;
            try {
                DateTime now = new DateTime(DateTimeZone.UTC);
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                while (retries < nrOfRetries) {
                    try {
                        importPmh(dataSet);
                        solrServer.deleteByQuery("europeana_collectionName:" + dataSet.getSpec() + " AND timestamp:[* TO " + fmt.print(now) + "]");
                        log.info("deleting orphaned entries from the SolrIndex for collection" + dataSet.getSpec());
                        break;
                    }
                    catch (SolrServerException e) {
                        if (retries < nrOfRetries) {
                            retries += 1;
                            log.info("unable to delete orphans. Retrying...");
                        }
                        else {
                            throw new SolrServerException(e);
                        }

                    }
                    catch (IOException e) {
                        if (retries < nrOfRetries) {
                            retries += 1;
                            log.info("unable to delete orphans. Retrying...");
                        }
                        else {
                            throw new IOException(e);
                        }
                    }
                }
                if (thread != null) {
                    log.info("Finished importing " + dataSet);
                    dataSet.setState(DataSetState.ENABLED); // funny, but this can enable before the indexed record count has been fully incremented
                }
                else {
                    log.info("Aborted importing " + dataSet);
                    dataSet.setState(DataSetState.INCOMPLETE);
                }
                dataSet.save();
            }
            catch (HarvindexingException e) {
                recordProblem(e);
            }
            catch (Exception e) {
                recordProblem(e);
            }
            finally {
                processors.remove(this);
                thread = null;
            }
        }

        private void recordProblem(Exception ex) {
            log.warn("Problem importing " + dataSet + ", to ERROR state.", ex);
            dataSet.setErrorState(ex.getMessage());
            dataSet.save();
        }

        private void importPmh(MetaRepo.DataSet dataSet) throws HarvindexingException, IOException, TransformerException, XMLStreamException, SolrServerException {
            String accessKeyString = accessKey.createKey("HARVINDEXER");
            String url = String.format(
                    "%s/oai-pmh?verb=ListRecords&metadataPrefix=%s&set=%s&accessKey=%s",
                    servicesUrl,
                    metadataPrefix,
                    dataSet.getSpec(),
                    accessKeyString
            );
            HttpMethod method = new GetMethod(url);
            httpClient.executeMethod(method);
            Indexer indexer = new Indexer(dataSet);
            InputStream inputStream = method.getResponseBodyAsStream();
            String resumptionToken = importXmlInternal(inputStream, indexer);
            while (!resumptionToken.isEmpty()) {
                log.info(String.format("So far %d records, resumption token %s", indexer.getRecordCount(), resumptionToken));
                method = new GetMethod(String.format(
                        "%s/oai-pmh?verb=ListRecords&resumptionToken=%s&accessKey=%s",
                        servicesUrl,
                        resumptionToken,
                        accessKeyString
                ));
                httpClient.executeMethod(method);
                inputStream = method.getResponseBodyAsStream();
                resumptionToken = importXmlInternal(inputStream, indexer);
                if (dataSet.getState() != DataSetState.INDEXING) {
                    break;
                }
                if (indexer.isFull()) {
                    log.info(String.format("Indexer full with %d records", indexer.getRecordCount()));
                    executor.execute(indexer);
                    indexer = new Indexer(dataSet);
                }
            }
            if (indexer.hasRecords()) {
                log.info(String.format("Harvest finished with %d records to index", indexer.getRecordCount()));
                executor.execute(indexer);
            }
        }

        private String importXmlInternal(InputStream inputStream, Indexer indexer) throws TransformerException, XMLStreamException, IOException, SolrServerException, HarvindexingException {
            Source source = new StreamSource(inputStream, "UTF-8");
            XMLStreamReader xml = inputFactory.createXMLStreamReader(source);
            String pmhId = null;
            String resumptionToken = "";
            int recordCount = 0;
            boolean isInMetadataBlock = false;
            long startTime = System.currentTimeMillis();
            Path path = new Path();
            SolrInputDocument solrInputDocument = null;
            while (thread != null) {
                switch (xml.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                        log.info("Document started");
                        break;

                    case XMLStreamConstants.START_ELEMENT:
                        if (isErrorElement(xml)) {
                            throw new HarvindexingException(xml.getElementText());
                        }
                        else if (!isInMetadataBlock && isPmhIdentifier(xml)) {
                            pmhId = xml.getElementText();
                        }
                        else if (isMetadataElement(xml)) {
                            isInMetadataBlock = true;
                        }
                        else if (isResumptionToken(xml)) {
                            resumptionToken = xml.getElementText();
                        }
                        else if (isRecordElement(xml) && isInMetadataBlock) {
                            path.push(Tag.create(xml.getName().getPrefix(), xml.getName().getLocalPart()));
                            solrInputDocument = new SolrInputDocument();
                            solrInputDocument.addField("delving_pmhId", pmhId);
                        }
                        else if (solrInputDocument != null) {
                            path.push(Tag.create(xml.getName().getPrefix(), xml.getName().getLocalPart()));
                            FieldDefinition fieldDefinition = getFieldDefinition(path, recordCount);
                            String text = xml.getElementText();
                            FieldDefinition.Validation validation = fieldDefinition.validation;
                            if (validation != null) {
                                if (validation.type) {
                                    DocType.get(text); // checking if it matches one of them
                                    SolrInputField objectField = solrInputDocument.getField("europeana_type");
                                    if (objectField != null) {
                                        break;
                                    }
                                }
                            }
                            if (text.length() > 10000) {
                                log.warn("Truncated value from " + text.length());
                                text = text.substring(0, 9999);
                            }
                            // language being ignored if (language != null) {...}
                            solrInputDocument.addField(fieldDefinition.getFieldNameString(), text);
                            if (xml.isEndElement()) {
                                path.pop();
                            }
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (isRecordElement(xml) && isInMetadataBlock && solrInputDocument != null) {
                            isInMetadataBlock = false;
                            if (recordCount > 0 && recordCount % 500 == 0) {
                                log.info(String.format("imported %d records in %s", recordCount, DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime)));
                            }
                            recordCount++;
                            Collection<Object> objectUrls = solrInputDocument.getFieldValues("europeana_object");
                            if (objectUrls != null && !objectUrls.isEmpty()) {
                                solrInputDocument.addField("europeana_hasDigitalObject", true);
                            }
                            else {
                                solrInputDocument.addField("europeana_hasDigitalObject", false);
                            }
                            if (!solrInputDocument.containsKey("europeana_collectionName")) {
                                solrInputDocument.addField("europeana_collectionName", dataSet.getSpec()); // todo: can't just use a string field name here
                            }
                            indexer.add(solrInputDocument);
//                            record.save(); todo: or something like it, to replace consoleDao.saveEuropeanaId(europeanaId);
                            solrInputDocument = null;
                            path.pop();
                        }
                        else if (isMetadataElement(xml)) {
                            isInMetadataBlock = false;
                        }
                        if (solrInputDocument != null) {
                            path.pop();
                            log.info("eid not null end: " + path);
                        }
                        break;

                    case XMLStreamConstants.END_DOCUMENT:
                        log.info(String.format("Document ended, fetched %d records", recordCount));
                        break;
                }
                if (!xml.hasNext()) {
                    break;
                }
                xml.next();
            }
            inputStream.close();
            return resumptionToken;
        }

        private FieldDefinition getFieldDefinition(Path path, int recordCount) throws HarvindexingException {
            FieldDefinition fieldDefinition = metadataModel.getRecordDefinition().getFieldDefinition(path);
            if (fieldDefinition == null) {
                throw new HarvindexingException("Field not recognized: " + path, recordCount);
            }
            return fieldDefinition;
        }

        private boolean isRecordElement(XMLStreamReader xml) {
            return "record".equals(xml.getName().getLocalPart());
        }

        private boolean isMetadataElement(XMLStreamReader xml) {
            return "metadata".equals(xml.getName().getLocalPart());
        }

        private boolean isPmhIdentifier(XMLStreamReader xml) {
            return "identifier".equals(xml.getName().getLocalPart());
        }

        private boolean isErrorElement(XMLStreamReader xml) {
            return "error".equals(xml.getName().getLocalPart());
        }

        private boolean isResumptionToken(XMLStreamReader xml) {
            return "resumptionToken".equals(xml.getName().getLocalPart());
        }
    }

    private static String exceptionToErrorString(HarvindexingException exception) {
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

    private class Indexer implements Runnable {
        private MetaRepo.DataSet dataSet;
        private List<SolrInputDocument> recordList = new ArrayList<SolrInputDocument>();

        private Indexer(MetaRepo.DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void add(SolrInputDocument record) {
            recordList.add(record);
        }

        public boolean hasRecords() {
            return !recordList.isEmpty();
        }

        public boolean isFull() {
            return recordList.size() >= chunkSize;
        }

        @Override
        public void run() {
            try {
                log.info("sending " + recordList.size() + " records to solr");
                solrServer.add(recordList);
                dataSet.incrementRecordsIndexed(recordList.size());
            }
            catch (SolrServerException e) {
                log.error("unable to index this batch");
                log.error(recordList.toString());
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int getRecordCount() {
            return recordList.size();
        }
    }
}
