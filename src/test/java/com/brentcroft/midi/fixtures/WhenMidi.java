package com.brentcroft.midi.fixtures;

import com.brentcroft.midi.MidiReader;
import com.brentcroft.midi.MidiInputSource;
import com.brentcroft.midi.MidiPlayer;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState;

import javax.sound.midi.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;

public class WhenMidi extends Stage< WhenMidi >
{
    @ProvidedScenarioState
    Exception exception;

    @ScenarioState
    Sequence sequence;

    public WhenMidi serialize_sequence_to_file( String filepath )
    {
        try ( FileWriter writer = new FileWriter( filepath ) )
        {
            MidiReader.getSerializer()
                    .transform(
                            new SAXSource(
                                    new MidiReader(),
                                    new MidiInputSource( sequence )
                            ),
                            new StreamResult( writer ) );
        }
        catch ( Exception e )
        {
            exception = e;
        }

        return self();
    }

    public WhenMidi play_sequence()
    {
        new MidiPlayer().play(sequence, true);

        return self();
    }
}
