# midi-reader
Transform arbitrary XML to midi.

In particular, given a CSV file with object detections:

  [Detections CSV file](../blob/master/src/test/resources/detections.csv)

When the following Jstl Template is applied:

  [Detections to MIDI Transformer](../blob/master/src/test/resources/camera-scenes.midi.xml)

Then the midi file exported is:

  [Detections converted to MIDI](../blob/master/src/test/resources/detections.midi)
