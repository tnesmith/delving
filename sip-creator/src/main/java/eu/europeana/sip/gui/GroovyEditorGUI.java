package eu.europeana.sip.gui;

import eu.europeana.sip.xml.NormalizationParser;
import eu.europeana.sip.xml.QNameBuilder;
import groovy.lang.Binding;
import groovy.util.Node;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.util.Iterator;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyEditorGUI extends JFrame {

    private static final String DEFAULT_INPUT_FILE = "core/src/test/sample-metadata/92001_Ag_EU_TELtreasures.xml"; // todo: replace
    private GroovyEditor groovyEditor;
    private JTextArea outputTextArea = new JTextArea();
    private NormalizationParser normalizationParser;
    private Node record;
    private Iterator<Node> nodeIterator;

    public GroovyEditorGUI(File inputFile) throws FileNotFoundException, XMLStreamException {
        super("Standalone Groovy editor");
        getContentPane().add(createMainPanel());
        QName recordRoot = QNameBuilder.createQName("record");
        normalizationParser = new NormalizationParser(new FileInputStream(inputFile), recordRoot);
        nodeIterator = normalizationParser.iterator();
        nextRecord();
        setSize(1200, 900);
    }

    private JComponent createMainPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(createSplitPane(), BorderLayout.CENTER);
        p.add(createNextButton(), BorderLayout.NORTH);
        return p;
    }

    private JComponent createNextButton() {
        JButton next = new JButton("Next");
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextRecord();
                groovyEditor.triggerExecution();
            }
        });
        return next;
    }

    private JComponent createSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        groovyEditor = new GroovyEditor(
                new GroovyEditor.Listener() {
                    @Override
                    public void update(String result) {
                        outputTextArea.setText(result);
                    }
                },
                new Source()
        );
        JScrollPane scroll;
        split.setTopComponent(scroll = new JScrollPane(groovyEditor));
        scroll.setPreferredSize(new Dimension(1200, 400));
        split.setBottomComponent(scroll = new JScrollPane(outputTextArea));
        scroll.setPreferredSize(new Dimension(1200, 400));
        split.setDividerLocation(0.5);
        return split;
    }

    private void nextRecord() {
        record = nodeIterator.next();
    }

    private class Source implements GroovyEditor.BindingSource {

        @Override
        public Binding createBinding(Writer writer) {
            MarkupBuilder builder = new MarkupBuilder(writer);
            NamespaceBuilder xmlns = new NamespaceBuilder(builder);
            Binding binding = new Binding();
            binding.setVariable("record", record);
            binding.setVariable("builder", builder);
            binding.setVariable("dc", xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
            binding.setVariable("dcterms", xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
            binding.setVariable("europeana", xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
            return binding;
        }
    }

    public static void main(String... args) throws FileNotFoundException, XMLStreamException {
        File file;
        if (args.length < 1) {
            file = new File(DEFAULT_INPUT_FILE);
        }
        else {
            file = new File(args[0]);
        }
        GroovyEditorGUI groovyEditorGUI = new GroovyEditorGUI(file);
        groovyEditorGUI.setVisible(true);
    }
}