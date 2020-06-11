package com.brentcroft.midi;

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

public class MidiReaderTest extends ScenarioTest< GivenMidi, WhenMidi, ThenMidi >
{
    int volume = 50;

    long tick = 1;
    int channel = 0;
    int channel2 = 14;
    int instrument = 0;
    int instrument2 = 23;

    int middleC = 60;
    int x = 30;

    String filepath = "target/snapshot.xml";


    @Test
    public void play_detections() throws IOException, SAXException, ParserConfigurationException
    {
        given()
                .sequence_from_file( "src/test/resources/camera-scenes.midi.xml" );

        when()
                .serialize_sequence_to_file( filepath );
    }


    @Test
    public void play_file() throws IOException, SAXException, ParserConfigurationException
    {
        given()
                .sequence_from_file( "src/test/resources/sample.midi.xml" );

        when()
                .serialize_sequence_to_file( filepath );

//        then()
//                .file_exists( filepath )
//                .parse_file( filepath );
    }

    @Test
    public void write_midi_xml_file() throws InvalidMidiDataException, IOException, SAXException, ParserConfigurationException
    {
        int res = 16;

        given()
                .sequence_with_resolution( res, 4 );

        given()
                .short_message_event( 1, ShortMessage.PROGRAM_CHANGE, channel, instrument, 1, tick )

                .short_message_event( 1, ShortMessage.NOTE_ON, channel, middleC, volume, tick )
                .short_message_event( 1, ShortMessage.NOTE_OFF, channel, middleC, volume, tick + 30 )

                .short_message_event( 1, ShortMessage.NOTE_ON, channel, x, volume, tick + 60 )
                .short_message_event( 1, ShortMessage.NOTE_OFF, channel, x, volume, tick + 120 );

//        given()
//                .short_message_event( 2, ShortMessage.PROGRAM_CHANGE, channel, instrument2, 1, tick )
//
//                .short_message_event( 2, ShortMessage.NOTE_ON, channel, x, volume, tick + 15 )
//                .short_message_event( 2, ShortMessage.NOTE_OFF, channel, x, volume, tick + 45 )
//
//                .short_message_event( 2, ShortMessage.NOTE_ON, channel, x, volume, tick + 75 )
//                .short_message_event( 2, ShortMessage.NOTE_OFF, channel, x, volume, tick + 145 )
        ;

        when()
                .serialize_sequence_to_file( filepath );

        then()
                .file_exists( filepath )
                .no_exception()
                .parse_file( filepath );
    }

    @Test
    public void play_sequence() throws InvalidMidiDataException
    {

        given()
                .sequence_with_resolution( 4, 4 );

        given()
                .short_message_event( 1, ShortMessage.PROGRAM_CHANGE, channel, instrument, 1, tick )

                .short_message_event( 1, ShortMessage.NOTE_ON, channel, middleC, volume, tick )
                .short_message_event( 1, ShortMessage.NOTE_OFF, channel, middleC, volume, tick + 30 )

                .short_message_event( 1, ShortMessage.NOTE_ON, channel, middleC, volume, tick + 60 )
                .short_message_event( 1, ShortMessage.NOTE_OFF, channel, middleC, volume, tick + 120 );

        given()
                .short_message_event( 2, ShortMessage.PROGRAM_CHANGE, channel2, instrument2, 1, tick )

                .short_message_event( 2, ShortMessage.NOTE_ON, channel2, x, volume, tick + 15 )
                .short_message_event( 2, ShortMessage.NOTE_OFF, channel2, x, volume, tick + 45 )

                .short_message_event( 2, ShortMessage.NOTE_ON, channel2, x, volume, tick + 75 )
                .short_message_event( 2, ShortMessage.NOTE_OFF, channel2, x, volume, tick + 145 )
        ;

        when()
                .play_sequence();

        then()
                .no_exception();
    }
}