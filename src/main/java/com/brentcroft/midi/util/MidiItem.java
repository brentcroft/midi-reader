package com.brentcroft.midi.util;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import static javax.sound.midi.Sequence.*;


public interface MidiItem
{
    enum TAG
    {
        SEQUENCER( "sequencer" ),
        SEQUENCE( "sequence" ),
        TRACK( "track" ),
        EVENT( "event" ),
        MIDI_DEVICE_INFO( "midi-device-info" ),
        INSTRUMENT( "instrument" ),
        NOTE( "note" ),
        TICK( "tick" ),
        START( "start" ),
        EXPORT( "export" );

        private final String tag;

        TAG( String tag )
        {
            this.tag = tag;
        }

        public boolean isTag( String t )
        {
            return tag.equals( t );
        }

        public boolean isTag( Element element )
        {
            return tag.equals( element.getTagName() );
        }

        public String getTag()
        {
            return tag;
        }
    }


    enum ATTR
    {
        RESOLUTION( "resolution" ),
        DIVISION_TYPE( "division-type" ),
        TRACKS( "tracks" ),
        TRACK( "track" ),
        TICK( "tick" ),
        COMMAND( "command" ),
        CHANNEL( "channel" ),
        DATA_1( "data_1" ),
        DATA_2( "data_2" ),
        TICK_LENGTH( "tick-length" ),
        MICROS_LENGTH( "micros-length" ),
        TYPE( "type" ),
        STATUS( "status" ),
        NAME( "name" ),

        LOOP_COUNT( "loop-count" ),
        TEMPO_FACTOR( "tempo-factor" ),
        TEMPO_BPM( "tempo-bpm" ),
        TEMPO_MPQ( "tempo-mpq" ),
        NOTE( "note" ),
        TICKS( "ticks" ),
        VOL( "vol" ),
        PROGRAM( "program" ),
        SOLO( "solo" ),
        MUTE( "mute" ),
        AT( "at" ),
        MICROS_POSITION( "micros-position" ),
        TICK_POSITION( "tick-position" ),
        FILE( "file" );

        private final String attr;


        ATTR( String attr )
        {
            this.attr = attr;
        }

        public boolean hasAttribute( Attributes atts )
        {
            return atts.getIndex( attr ) > - 1;
        }

        public boolean hasAttribute( Element element )
        {
            return element.hasAttribute( attr );
        }


        public String getAttribute()
        {
            return attr;
        }

        public String getAttribute( Element element )
        {
            return hasAttribute( element ) ? element.getAttribute( attr ) : null;
        }

        public String getAttribute( Attributes atts )
        {
            return atts.getValue( attr );
        }

        public void setAttribute( AttributesImpl atts, String namespace, String value )
        {
            atts.addAttribute( namespace, attr, attr, "", value == null ? "" : value );
        }
    }




    static float getDivisionType( String dt )
    {
        switch ( dt.toUpperCase() )
        {
            case "SMPTE_24":
                return SMPTE_24;
            case "SMPTE_25":
                return SMPTE_25;
            case "SMPTE_30DROP":
                return SMPTE_30DROP;
            case "SMPTE_30":
                return SMPTE_30;
            default:
                return PPQ;
        }
    }

    static String getDivisionType( float dt )
    {
        return dt == SMPTE_24
               ? "SMPTE_24"
               : dt == SMPTE_25
                 ? "SMPTE_25"
                 : dt == SMPTE_30DROP
                   ? "SMPTE_30DROP"
                   : dt == SMPTE_30
                     ? "SMPTE_30"
                     : "PPQ";
    }


    static String getStatusName( int status )
    {
        switch ( status )
        {
            case SysexMessage.SYSTEM_EXCLUSIVE:
                return "SYSTEM_EXCLUSIVE";
            case SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE:
                return "SPECIAL_SYSTEM_EXCLUSIVE";

            case ShortMessage.NOTE_OFF:
                return "NOTE_OFF";
            case ShortMessage.NOTE_ON:
                return "NOTE_ON";
            case ShortMessage.PROGRAM_CHANGE:
                return "PROGRAM_CHANGE";
            case ShortMessage.ACTIVE_SENSING:
                return "ACTIVE_SENSING";
            case ShortMessage.CHANNEL_PRESSURE:
                return "CHANNEL_PRESSURE";
            case ShortMessage.CONTINUE:
                return "CONTINUE";
            case ShortMessage.CONTROL_CHANGE:
                return "CONTROL_CHANGE";

            //case ShortMessage.END_OF_EXCLUSIVE: return "END_OF_EXCLUSIVE";
            case ShortMessage.MIDI_TIME_CODE:
                return "MIDI_TIME_CODE";
            case ShortMessage.PITCH_BEND:
                return "PITCH_BEND";
            case ShortMessage.POLY_PRESSURE:
                return "POLY_PRESSURE";
            case ShortMessage.SONG_POSITION_POINTER:
                return "SONG_POSITION_POINTER";
            case ShortMessage.SONG_SELECT:
                return "SONG_SELECT";
            case ShortMessage.START:
                return "START";

            case ShortMessage.STOP:
                return "STOP";
//            case ShortMessage.SYSTEM_RESET:
//                return "SYSTEM_RESET";
            case ShortMessage.TIMING_CLOCK:
                return "TIMING_CLOCK";
            case ShortMessage.TUNE_REQUEST:
                return "TUNE_REQUEST";

            case MetaMessage.META:
                return "META";
        }

        return Integer.toHexString( status );
    }


    static int getStatus( String status )
    {
        switch ( status )
        {
            case "SYSTEM_EXCLUSIVE":
                return SysexMessage.SYSTEM_EXCLUSIVE;
            case "SPECIAL_SYSTEM_EXCLUSIVE":
                return SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE;

            case "NOTE_OFF":
                return ShortMessage.NOTE_OFF;
            case "NOTE_ON":
                return ShortMessage.NOTE_ON;
            case "PROGRAM_CHANGE":
                return ShortMessage.PROGRAM_CHANGE;
            case "ACTIVE_SENSING":
                return ShortMessage.ACTIVE_SENSING;
            case "CHANNEL_PRESSURE":
                return ShortMessage.CHANNEL_PRESSURE;
            case "CONTINUE":
                return ShortMessage.CONTINUE;
            case "CONTROL_CHANGE":
                return ShortMessage.CONTROL_CHANGE;

            case "END_OF_EXCLUSIVE":
                return ShortMessage.END_OF_EXCLUSIVE;
            case "MIDI_TIME_CODE":
                return ShortMessage.MIDI_TIME_CODE;
            case "PITCH_BEND":
                return ShortMessage.PITCH_BEND;
            case "POLY_PRESSURE":
                return ShortMessage.POLY_PRESSURE;
            case "SONG_POSITION_POINTER":
                return ShortMessage.SONG_POSITION_POINTER;
            case "SONG_SELECT":
                return ShortMessage.SONG_SELECT;
            case "START":
                return ShortMessage.START;

            case "STOP":
                return ShortMessage.STOP;
            case "SYSTEM_RESET":
                return ShortMessage.SYSTEM_RESET;
            case "TIMING_CLOCK":
                return ShortMessage.TIMING_CLOCK;
            case "TUNE_REQUEST":
                return ShortMessage.TUNE_REQUEST;

            case "META":
                return MetaMessage.META;
        }

        // assume hex
        return Integer.valueOf( status, 16 );
    }
}
