package com.brentcroft.midi.fixtures;

import com.brentcroft.midi.MidiWriter;
import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.MapBindings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ScenarioState;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ThenMidi extends Stage< ThenMidi >
{
    @ScenarioState
    Exception exception;

    public ThenMidi file_exists( String filePath )
    {
        assertTrue( new File( filePath ).exists() );

        return self();
    }

    public ThenMidi no_exception()
    {
        assertNull( "Unexpected exception: " + exception, exception );

        return self();
    }

    public ThenMidi parse_file( String filepath ) throws ParserConfigurationException, SAXException, IOException
    {
        JstlDocument jstlDocument = new JstlDocument();

        jstlDocument.setContentHandler(
                new MidiWriter( jstlDocument.getBindings() )
        );

        jstlDocument.setDocument(
                DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse( filepath ) );

        jstlDocument.renderEvents();

        return self();
    }
}
