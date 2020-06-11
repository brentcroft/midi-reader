package com.brentcroft.midi;


import com.brentcroft.midi.util.MidiItem;
import com.brentcroft.tools.jstl.ContextValueMapper;
import com.brentcroft.tools.jstl.MapBindings;
import lombok.Getter;
import lombok.Setter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.sound.midi.Sequence.PPQ;

public class MidiWriter extends DefaultHandler implements MidiItem
{
    @Setter
    @Getter
    private ContextValueMapper valueMapper;

    @Setter
    @Getter
    private MapBindings bindings;

    @Setter
    @Getter
    private Sequence sequence;

    @Setter
    @Getter
    private Sequencer sequencer;


    private int trackNo = - 1;
    private int channelNo = - 1;
    private int noteNo = - 1;

    private Stack< Integer > ticks = new Stack<>();

    public MidiWriter(MapBindings bindings)
    {
        this.bindings = bindings;
    }


    public void startDocument() throws SAXException
    {
        ticks.push( 0 );
    }


    public void startElement( String uri, String localName,
                              String qName, Attributes attributes )
            throws SAXException
    {
        if ( TAG.MIDI_DEVICE_INFO.isTag( qName ) )
        {
            for ( MidiDevice.Info info : MidiSystem.getMidiDeviceInfo() )
            {
                System.out.println( format( "name: %s, description: %s, version: %s", info.getName(), info.getVendor(), info.getVersion() ) );
            }
        }
        else if ( TAG.SEQUENCER.isTag( qName ) )
        {
            try
            {
                sequencer = MidiSystem.getSequencer();

                System.out.println( format( "new sequencer: %s", sequencer ) );

                if ( nonNull( sequence ) )
                {
                    sequencer.setSequence( sequence );
                }

                if ( ATTR.TEMPO_BPM.hasAttribute( attributes ) )
                {
                    sequencer.setTempoInBPM( Float.parseFloat( mapKeyValue( ATTR.TEMPO_BPM, attributes ) ) );
                }

                if ( ATTR.TEMPO_MPQ.hasAttribute( attributes ) )
                {
                    sequencer.setTempoInMPQ( Float.parseFloat( mapKeyValue( ATTR.TEMPO_MPQ, attributes ) ) );
                }

                if ( ATTR.LOOP_COUNT.hasAttribute( attributes ) )
                {
                    sequencer.setLoopCount( Integer.parseInt( mapKeyValue( ATTR.LOOP_COUNT, attributes ) ) );
                }

                if ( ATTR.TEMPO_FACTOR.hasAttribute( attributes ) )
                {
                    sequencer.setTempoFactor( Float.parseFloat( mapKeyValue( ATTR.TEMPO_FACTOR, attributes ) ) );
                }

                sequencer.open();
            }
            catch ( MidiUnavailableException | InvalidMidiDataException e )
            {
                throw new SAXException( e );
            }

        }
        else if ( TAG.START.isTag( qName ) )
        {
            if ( ATTR.MICROS_POSITION.hasAttribute( attributes ) )
            {
                sequencer.setMicrosecondPosition( mapLongOr( ATTR.MICROS_POSITION, attributes, 0 ) );
            }
            if ( ATTR.TICK_POSITION.hasAttribute( attributes ) )
            {
                sequencer.setTickPosition( mapLongOr( ATTR.TICK_POSITION, attributes, 0 ) );
            }

            sequencer.start();
        }
        else if ( TAG.EXPORT.isTag( qName ) )
        {
            if ( isNull( sequence ) )
            {
                throw new SAXException( "Received export but no sequence." );
            }
            else if ( ! ATTR.FILE.hasAttribute( attributes ) )
            {
                throw new SAXException( "Export has no file attribute" );
            }

            int midiFileType = mapIntOr( ATTR.TYPE, attributes, 1 );

            try
            {
                MidiSystem.write( sequence, midiFileType, new File( ATTR.FILE.getAttribute( attributes ) ) );
            }
            catch ( IOException e )
            {
                throw new SAXException( e );
            }
        }
        else if ( TAG.SEQUENCE.isTag( qName ) )
        {
            if ( isNull( sequence ) )
            {
                int resolution = mapIntOr( ATTR.RESOLUTION, attributes, 10 );

                float divisionType = ATTR.DIVISION_TYPE.hasAttribute( attributes )
                                     ? MidiItem.getDivisionType( mapKeyValue( ATTR.DIVISION_TYPE, attributes ) )
                                     : PPQ;

                int tracks = mapIntOr( ATTR.TRACKS, attributes, 1 );

                try
                {
                    sequence = new Sequence( divisionType, resolution, tracks );

                    if ( nonNull( sequencer ) )
                    {
                        sequencer.setSequence( sequence );
                    }
                }
                catch ( Exception ex )
                {
                    throw new SAXException( format( "Failed to create sequence: %s", ex.getMessage() ), ex );
                }
            }
        }
        else if ( TAG.TRACK.isTag( qName ) )
        {
            trackNo = mapIntOr( ATTR.TRACK, attributes, trackNo );
            channelNo = mapIntOr( ATTR.CHANNEL, attributes, channelNo );

            if ( ATTR.SOLO.hasAttribute( attributes ) )
            {
                sequencer.setTrackSolo( trackNo, Boolean.parseBoolean( ATTR.SOLO.getAttribute( attributes ) ) );
            }

            if ( ATTR.MUTE.hasAttribute( attributes ) )
            {
                sequencer.setTrackMute( trackNo, Boolean.parseBoolean( ATTR.MUTE.getAttribute( attributes ) ) );
            }

        }
        else if ( TAG.TICK.isTag( qName ) )
        {
            ticks.push( mapIntOr( ATTR.AT, attributes, ticks.isEmpty() ? 0 : ticks.peek() ) );

            if ( nonNull( bindings ) )
            {
                bindings.withEntry( "$tick", ticks.peek() );
            }
        }
        else if ( TAG.TEMPO.isTag( qName ) )
        {
            long tick = mapLongOr( ATTR.TICK, attributes, ticks.isEmpty() ? 0 : ticks.peek() );
            long bpm = mapLongOr( ATTR.BPM, attributes, 60 );

            // microseconds per quarternote
            long mpqn = ( 60 * 1000 * 1000 ) / bpm;

            // create the tempo byte array
            byte[] array = new byte[]{0, 0, 0};

            for ( int i = 0; i < 3; i++ )
            {
                int shift = ( 3 - 1 - i ) * 8;
                array[ i ] = ( byte ) ( mpqn >> shift );
            }

            try
            {
                MetaMessage setTempo = new MetaMessage( 81, array, 3 );

                for ( Track track : sequence.getTracks() )
                {
                    track.add( new MidiEvent( setTempo, tick ) );
                }
            }
            catch ( Exception ex )
            {
                throw new SAXException( format( "Failed to create tempo: %s", ex.getMessage() ), ex );
            }
        }
        else if ( TAG.INSTRUMENT.isTag( qName ) )
        {
            noteNo = mapIntOr( ATTR.NOTE, attributes, noteNo );

            if ( ATTR.PROGRAM.hasAttribute( attributes ) )
            {
                try
                {
                    long tick = mapLongOr( ATTR.TICK, attributes, ticks.isEmpty() ? 0 : ticks.peek() );

                    MidiMessage programChange = new ShortMessage(
                            ShortMessage.PROGRAM_CHANGE,
                            mapIntOr( ATTR.CHANNEL, attributes, channelNo ),
                            mapIntOr( ATTR.PROGRAM, attributes, 0 ),
                            0 );

                    sequence
                            .getTracks()[ trackNo ]
                            .add(
                                    new MidiEvent(
                                            programChange,
                                            tick
                                    )
                            );
                }
                catch ( InvalidMidiDataException e )
                {
                    throw new SAXException( e );
                }
            }
        }
        else if ( TAG.NOTE.isTag( qName ) )
        {
            if ( isNull( sequence ) )
            {
                throw new SAXException( "Received note but sequence is null." );
            }

            try
            {
                long tick = mapLongOr( ATTR.TICK, attributes, ticks.isEmpty() ? 0 : ticks.peek() );
                long ticks = mapLongOr( ATTR.TICKS, attributes, 1 );

                int channel = mapIntOr( ATTR.CHANNEL, attributes, channelNo );
                int data1 = mapIntOr( ATTR.NOTE, attributes, noteNo );
                int data2 = mapIntOr( ATTR.VOL, attributes, 100 );

                MidiMessage messageOn = new ShortMessage( ShortMessage.NOTE_ON, channel, data1, data2 );
                MidiMessage messageOff = new ShortMessage( ShortMessage.NOTE_OFF, channel, data1, 0 );

                sequence
                        .getTracks()[ trackNo ]
                        .add(
                                new MidiEvent(
                                        messageOn,
                                        tick
                                )
                        );
                sequence
                        .getTracks()[ trackNo ]
                        .add(
                                new MidiEvent(
                                        messageOff,
                                        tick + ticks
                                )
                        );
            }
            catch ( InvalidMidiDataException e )
            {
                throw new SAXException( e );
            }

        }
        else if ( TAG.EVENT.isTag( qName ) )
        {
            if ( isNull( sequence ) )
            {
                throw new SAXException( "Received event but sequence is null." );
            }

            try
            {
                long tick = mapLongOr( ATTR.TICK, attributes, ticks.isEmpty() ? 0 : ticks.peek() );

                int status = ATTR.STATUS.hasAttribute( attributes )
                             ? mapIntOr( ATTR.STATUS, attributes, 0 )
                             : ATTR.NAME.hasAttribute( attributes )
                               ? MidiItem.getStatus( mapKeyValue( ATTR.NAME, attributes ) )
                               : - 1;

                int channel = mapIntOr( ATTR.CHANNEL, attributes, channelNo );
                int data1 = mapIntOr( ATTR.DATA_1, attributes, noteNo );
                int data2 = mapIntOr( ATTR.DATA_2, attributes, 0 );

                MidiMessage message = ( status == MetaMessage.META )
                                      ? new MetaMessage()
                                      : ( status == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE || status == SysexMessage.SYSTEM_EXCLUSIVE )
                                        ? new SysexMessage()
                                        : new ShortMessage( status, channel, data1, data2 );

                sequence
                        .getTracks()[ trackNo ]
                        .add(
                                new MidiEvent(
                                        message,
                                        tick
                                )
                        );
            }
            catch ( InvalidMidiDataException e )
            {
                throw new SAXException( e );
            }
        }
    }

    private int mapIntOr( ATTR attr, Attributes attributes, int or ) throws SAXException
    {
        return attr.hasAttribute( attributes )
               ? Integer.parseInt( mapKeyValue( attr, attributes ) )
               : or;
    }

    private long mapLongOr( ATTR attr, Attributes attributes, long or ) throws SAXException
    {
        return attr.hasAttribute( attributes )
               ? Long.parseLong( mapKeyValue( attr, attributes ) )
               : or;
    }


    private float mapFloatOr( ATTR attr, Attributes attributes, float or ) throws SAXException
    {
        return attr.hasAttribute( attributes )
               ? Float.parseFloat( mapKeyValue( attr, attributes ) )
               : or;
    }


    private String mapKeyValue( ATTR attr, Attributes attributes ) throws SAXException
    {
        String value = attr.getAttribute( attributes );

        if ( value.isEmpty() )
        {
            throw new SAXException( format( "Attribute [%s] is empty", attr ) );
        }

        return nonNull( valueMapper )
               ? valueMapper.map( attr.getAttribute(), value )
               : value;
    }


    public void endElement( String uri, String localName, String qName ) throws SAXException
    {
        if ( TAG.SEQUENCER.isTag( qName ) )
        {
            while ( sequencer.isRunning() )
            {
                try
                {
                    Thread.sleep( 100 );
                }
                catch ( InterruptedException e )
                {
                    throw new SAXException( e );
                }
            }

            sequencer.stop();
            sequencer.close();
            sequencer = null;
        }
        else if ( TAG.TICK.isTag( qName ) )
        {
            ticks.pop();
        }
        else if ( TAG.TRACK.isTag( qName ) )
        {
            trackNo = - 1;
            channelNo = - 1;
        }
        else if ( TAG.INSTRUMENT.isTag( qName ) )
        {
            noteNo = - 1;
        }
    }

    @Override
    public void characters( char[] ch, int start, int length ) throws SAXException
    {
    }
}
