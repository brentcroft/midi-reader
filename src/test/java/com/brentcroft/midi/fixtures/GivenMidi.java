package com.brentcroft.midi.fixtures;

import com.brentcroft.midi.MidiWriter;
import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.MapBindings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.xml.sax.SAXException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

import static javax.sound.midi.Sequence.PPQ;

public class GivenMidi extends Stage< GivenMidi >
{
    @ProvidedScenarioState
    Sequence sequence;


    public GivenMidi sequence_with_resolution( int resolution, int numTracks ) throws InvalidMidiDataException
    {
        sequence = new Sequence( PPQ, resolution, numTracks );

        return self();
    }

    public GivenMidi short_message_event( int track, int command, int channel, int data1, int data2, long tick ) throws InvalidMidiDataException
    {
        sequence
                .getTracks()[ track ]
                .add(
                        new MidiEvent(
                                new ShortMessage( command, channel, data1, data2 ), tick ) );
        return self();
    }

    public GivenMidi sequence_from_file( String filepath ) throws ParserConfigurationException, IOException, SAXException
    {
        JstlDocument jstlDocument = new JstlDocument();

        jstlDocument.getBindings().put( "model", new MapBindings() );

        MidiWriter mw  = new MidiWriter();
        mw.setBindings( jstlDocument.getBindings() );

        jstlDocument.setContentHandler( mw );

        jstlDocument.setDocument(
                DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse( filepath ) );

        jstlDocument.renderEvents();

        sequence = mw.getSequence();

        return self();
    }
}
