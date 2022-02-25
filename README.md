# Word counter

## Intro
Code consists of two parts:
1. A stream that will read events one line at a time from "events.txt" file,
every 500 millis, and perform a windowed word count.
2. An HTTP server that exposes a GET endpoint on http://localhost:8080/word/{word}
that will respond with the current word count per event type
with a 15 second sliding time window.
There's a simple script that CURLs the endpoint: `get-wordcount.sh`.

## Layout
Main code logic is in `WordCount.scala`.

## Why not use the binary?
I couldn't run the binary, so I created a simple `EventEmitter` that generates the events and writes it to
`events.txt`. I hope that's fine. I made sure to add some garbage data as well.

## How to run the Program
Enter the `sbt` shell and type `runProgram`.
You can then run the script on the words that are printed out.


