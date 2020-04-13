package com.brentcroft.midi;

import com.brentcroft.midi.fixtures.GivenCameraMidi;
import com.brentcroft.midi.fixtures.GivenMidi;
import com.brentcroft.midi.fixtures.ThenMidi;
import com.brentcroft.midi.fixtures.WhenMidi;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class DetectionMidiTest extends ScenarioTest< GivenCameraMidi, WhenMidi, ThenMidi >
{

    @Test
    public void play_file() throws IOException, SAXException, ParserConfigurationException
    {


        given()
                .detections_from_file( "src/test/resources/detections.csv" );


//        then()
//                .file_exists( filepath )
//                .parse_file( filepath );
    }

}