package com.brentcroft.midi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.xml.sax.InputSource;

import javax.sound.midi.Sequence;

@Getter
@AllArgsConstructor
public class MidiInputSource extends InputSource
{
    private final Sequence sequence;
}
