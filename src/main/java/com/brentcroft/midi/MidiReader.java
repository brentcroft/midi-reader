package com.brentcroft.midi;


import com.brentcroft.midi.util.AbstractXMLReader;
import com.brentcroft.midi.util.MidiItem;
import lombok.Getter;
import lombok.Setter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.sound.midi.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;

@Getter
@Setter
public class MidiReader extends AbstractXMLReader implements MidiItem
{
    private static final String NAMESPACE_URI = "";

    public void parse( InputSource input ) throws SAXException
    {
        if ( input instanceof MidiInputSource )
        {
            parse( ( ( MidiInputSource ) input ).getSequence() );
        }
    }


    public void parse( Sequencer sequencer ) throws SAXException
    {
        ContentHandler contentHandler = getContentHandler();
    }

    public void parse( Sequence sequence ) throws SAXException
    {
        ContentHandler contentHandler = getContentHandler();

        if ( contentHandler == null )
        {
            throw new SAXException( "No ContentHandler." );
        }
        else if ( sequence == null )
        {
            throw new SAXException( "No Sequence." );
        }

        contentHandler.startDocument();

        String tag = TAG.SEQUENCE.getTag();
        final AttributesImpl atts = new AttributesImpl();

        ATTR.DIVISION_TYPE.setAttribute( atts, NAMESPACE_URI, MidiItem.getDivisionType( sequence.getDivisionType() ) );
        ATTR.RESOLUTION.setAttribute( atts, NAMESPACE_URI, String.valueOf( sequence.getResolution() ) );
        ATTR.TRACKS.setAttribute( atts, NAMESPACE_URI, String.valueOf( sequence.getTracks().length ) );
        ATTR.TICK_LENGTH.setAttribute( atts, NAMESPACE_URI, String.valueOf( sequence.getTickLength() ) );
        ATTR.MICROS_LENGTH.setAttribute( atts, NAMESPACE_URI, String.valueOf( sequence.getMicrosecondLength() ) );

        contentHandler.startElement( NAMESPACE_URI, tag, tag, atts );

        processTracks( sequence, contentHandler );

        contentHandler.endElement( NAMESPACE_URI, tag, tag );
        contentHandler.endDocument();
    }


    private void processTracks( Sequence parent, ContentHandler contentHandler ) throws SAXException
    {
        Track[] tracks = parent.getTracks();

        // items might be null
        if ( isNull( tracks ) || tracks.length == 0 )
        {
            return;
        }

        for ( int i = 0, n = tracks.length; i < n; i++ )
        {
            final Track track = tracks[ i ];

            AttributesImpl atts = new AttributesImpl();
            ATTR.TRACK.setAttribute( atts, NAMESPACE_URI, String.valueOf( i ) );
            final String tag = TAG.TRACK.getTag();

            contentHandler.startElement( NAMESPACE_URI, tag, tag, atts );

            processTrack( track, contentHandler );

            contentHandler.endElement( NAMESPACE_URI, tag, tag );
        }
    }

    private void processTrack( Track track, ContentHandler contentHandler ) throws SAXException
    {
        List< MidiEvent > events = IntStream
                .range( 0, track.size() )
                .mapToObj( track::get )
                .collect( Collectors.toList() );


        for ( MidiEvent event : events )
        {
            AttributesImpl atts = new AttributesImpl();
            //ATTR.TRACK.setAttribute( atts, NAMESPACE_URI, String.valueOf( i ) );
            ATTR.TICK.setAttribute( atts, NAMESPACE_URI, String.valueOf( event.getTick() ) );

            MidiMessage message = event.getMessage();

            ATTR.STATUS.setAttribute( atts, NAMESPACE_URI, String.valueOf( message.getStatus() ) );
            ATTR.NAME.setAttribute( atts, NAMESPACE_URI, MidiItem.getStatusName( message.getStatus() ) );

            if ( message instanceof ShortMessage )
            {
                ShortMessage sm = ( ShortMessage ) message;

                //ATTR.COMMAND.setAttribute( atts, NAMESPACE_URI, String.valueOf( sm.getCommand() ) );
                ATTR.CHANNEL.setAttribute( atts, NAMESPACE_URI, String.valueOf( sm.getChannel() ) );
                ATTR.DATA_1.setAttribute( atts, NAMESPACE_URI, String.valueOf( sm.getData1() ) );
                ATTR.DATA_2.setAttribute( atts, NAMESPACE_URI, String.valueOf( sm.getData2() ) );
            }
            else if ( message instanceof MetaMessage )
            {
                MetaMessage mm = ( MetaMessage ) message;

                ATTR.TYPE.setAttribute( atts, NAMESPACE_URI, String.valueOf( mm.getType() ) );
            }

            final String tag = TAG.EVENT.getTag();

            contentHandler.startElement( NAMESPACE_URI, tag, tag, atts );
            contentHandler.endElement( NAMESPACE_URI, tag, tag );
        }
    }


    public static void configureTransformer( Transformer transformer, boolean omitXmlDeclaration )
    {
        transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

        if ( omitXmlDeclaration )
        {
            transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        }

        //transformer.setOutputProperty( OutputKeys.CDATA_SECTION_ELEMENTS, TAGS_FOR_CATA_TEXT );

        transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );

        final int indent = 4;

        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", Integer.toString( indent ) );
    }

    public static Transformer getSerializer() throws TransformerConfigurationException
    {
        Transformer serializer = TransformerFactory.newInstance().newTransformer();

        configureTransformer( serializer, true );

        return serializer;
    }
}
