package com.brentcroft.midi;

import com.brentcroft.tools.jstl.JstlTemplateManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.IntStream;

import static java.lang.String.format;

@Getter
@Setter
@AllArgsConstructor
@Log
public class CameraScene
{
    public static void install( JstlTemplateManager jstl )
    {
        try
        {
            jstl
                    .getELTemplateManager()
                    .mapFunction( "c:camera_scenes_from_csv", CameraScene.class.getMethod( "fromCsv", String.class ) );

            log.info( () -> "Installed function: c:camera_scenes_from_csv( filepath )" );
        }
        catch ( NoSuchMethodException e )
        {
            e.printStackTrace();
        }
    }

    private LocalDate date;
    private LocalTime time;
    private String folder;
    private String filename;
    private ImageSize size;
    private List< Detection > detections;


    @Getter
    @Setter
    @AllArgsConstructor
    public static class ImageSize
    {
        private int width;
        private int height;
        private int depth;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Box
    {
        private int xmin;
        private int ymin;
        private int xmax;
        private int ymax;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class Detection
    {
        private String category;
        private double score;
        private Box box;
    }

    public static List< CameraScene > fromCsv( String filepath ) throws FileNotFoundException
    {
        List< CameraScene > scenes = new ArrayList<>();

        Map< String, Integer > header = new HashMap<>();

        try ( Scanner scanner = new Scanner( new File( filepath ) ) )
        {
            String lastFolderFilename = null;
            List< Detection > detections = null;

            while ( scanner.hasNextLine() )
            {
                String line = scanner.nextLine();

                String[] columns = line.split( "\\s*,\\s*" );

                if ( columns.length < 2 )
                {
                    continue;
                }

                if ( header.isEmpty() )
                {
                    IntStream
                            .range( 0, columns.length )
                            .forEach( i -> {
                                header.put( columns[ i ], i );
                            } );
                }
                else
                {
                    LocalDate date = null;
                    LocalTime time = null;
//                    LocalDate date = LocalDate.parse( columns[ header.get( "folder" ) ] );
//                    LocalTime time = LocalTime
//                            .parse(
//                                    columns[ header.get( "filename" ) ]
//                                            .substring( 0, 12 )
//                                            .replaceAll( "-", ":" )
//                                            .replaceAll( "_", "." ) );
                    String folder = columns[ header.get( "folder" ) ];
                    String filename = columns[ header.get( "filename" ) ];

                    String newFolderFilename = format( "%s-%s", folder, filename );

                    Box box = new Box(
                            Integer.parseInt( columns[ header.get( "xmin" ) ] ),
                            Integer.parseInt( columns[ header.get( "ymin" ) ] ),
                            Integer.parseInt( columns[ header.get( "xmax" ) ] ),
                            Integer.parseInt( columns[ header.get( "ymax" ) ] )
                    );

                    Detection detection = new Detection(
                            columns[ header.get( "name" ) ],
                            Double.parseDouble( columns[ header.get( "score" ) ] ),
                            box
                    );


                    if ( newFolderFilename.equals( lastFolderFilename ) )
                    {
                        // add to last detections
                        detections.add( detection );
                    }
                    else
                    {
                        lastFolderFilename = newFolderFilename;

                        // new detections
                        detections = new ArrayList<>();

                        detections.add( detection );

                        ImageSize size = new ImageSize(
                                Integer.parseInt( columns[ header.get( "width" ) ] ),
                                Integer.parseInt( columns[ header.get( "height" ) ] ),
                                3
                        );

                        CameraScene scene = new CameraScene( date, time, folder, filename, size, detections );

                        scenes.add( scene );
                    }
                }
            }
        }

        return scenes;
    }
}
