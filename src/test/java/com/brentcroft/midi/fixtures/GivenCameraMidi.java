package com.brentcroft.midi.fixtures;

import com.brentcroft.midi.CameraScene;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static javax.sound.midi.Sequence.PPQ;

public class GivenCameraMidi extends Stage< GivenCameraMidi >
{
    @ProvidedScenarioState
    List<CameraScene> scenes;



    public GivenCameraMidi detections_from_file( String filepath ) throws FileNotFoundException
    {
        scenes = CameraScene.fromCsv( filepath );

        return self();
    }
}
