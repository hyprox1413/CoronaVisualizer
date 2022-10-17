import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.effects.*; 
import ddf.minim.signals.*; 
import ddf.minim.spi.*; 
import ddf.minim.ugens.*; 
import processing.video.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class coronaEQ extends PApplet {









float frames = 0;
int dotDepth = 10;

Minim minim;
File musicFolder;
File[] music;
ArrayList<File> songsNotPlayed;
AudioPlayer song;
ArrayList<File> songQueue;
int songPlaying = 0;
int framesPlayed = 0;
int playlistLength;

FFT fft;
int bands = 512;
Bar[] bars = new Bar[bands];
int mem = 20;
int roof = 32;
Bar amp;

class Bar {
  float[] history;
  int memory;
  Bar(int m) {
    memory = m;
    history = new float[memory];
    for (int i = 0; i < history.length; i ++) {
      history[i] = 0;
    }
  }

  public void update() {
    for (int i = history.length - 1; i > 0; i --) {
      history[i] = history[i - 1];
    }
  }

  public float val() {
    float total = 0;
    for (int i = 0; i < history.length; i ++) {
      total += history[i];
    }
    return total / history.length;
  }
}

public void checkSongOver() {
  framesPlayed ++;
  //println(framesPlayed + " " + song.duration() * 60);
  if (!song.isPlaying()) {
    skipSong();
  }
}

public void mouseClicked() {
  if (mouseButton == LEFT) {
    skipSong();
  } else if (mouseButton == RIGHT) {
    rewindSong();
  }
}

public void setup() {
  
  background(255);
  minim = new Minim(this);
  musicSetup();
  queueAdd();
  playSong();
  fft = new FFT(song.bufferSize(), song.sampleRate());

  amp = new Bar(30);
  noCursor();
  frameRate(60);
}

public void draw() {
  background(255);
  fft.forward(song.mix);
  amp.history[0] = (song.mix.level()) * 3;
  for (int i = 0; i < bars.length; i ++) {
    bars[i] = new Bar(mem);
  }
  for (int i = bands / roof - 1; i >= 0; i--) { 
    bars[i].history[0] = fft.getBand(i) * 4 * (i + 1) / (bands / roof + 1);
  }
  strokeWeight(10);
  stroke(0);
  noFill();
  ellipse(width/2, height/2, 200 * (3 + 1 * amp.val()), 200 * (3 + 1 * amp.val()));
  corona(width/2, height/2, 100 * (3 + 1 * amp.val()), frames, true);
  corona(width/2, height/2, 100 * (3 + 1 * amp.val()), frames, false);
  dots();
  for (int i = bands / roof - 1; i >= 0; i--) { 
    bars[i].update();
  }
  println(amp.val());
  amp.update();
  frames -= amp.val()/50;
}

public void dots() {
  for (float i = dotDepth - 1; i >= 0; i --) {
    for (int j = 0; j < bands/roof; j ++) {
      noStroke();
      fill(0, 255 * (bars[j].val() / 20 > i / dotDepth ? dotDepth * (bars[j].val() / 20 - i / dotDepth) : 0));
      pushMatrix();
      translate(width/2, height/2);
      scale((i+1)*(i+1)/dotDepth/dotDepth * (3 + 1 * amp.val()) / 4);
      rotate(j*roof*TAU/bands - TAU/4);
      ellipse(275, 0, 50, 50);
      popMatrix();
    }
  }
}

public void corona(float x, float y, float r, float theta, boolean o) {
  int lines = 30;
  for (int i = 0; i < lines; i++) {
    pushMatrix();
    translate(x, y);
    rotate(i*TAU/lines + (o?1:-1) * theta);
    strokeWeight(10);
    stroke(0);
    line(r, 0, r, (o?1:-1) * 10000);
    popMatrix();
  }
}
public void musicSetup() {
  musicFolder = new File(dataPath("")+"/music");
  music = musicFolder.listFiles();
  songsNotPlayed = new ArrayList<File>();
  songQueue = new ArrayList<File>();
}

public void queueAdd() {
  songQueue = new ArrayList<File>();
  for (File f : music) {
    songsNotPlayed.add(f);
  }
  playlistLength = songsNotPlayed.size();
  while (!songsNotPlayed.isEmpty()) {
    int songNum = (int) (random(1) * songsNotPlayed.size());
    songQueue.add(songsNotPlayed.get(songNum));
    songsNotPlayed.remove(songNum);
  }
}

public void playSong() {
  if(songPlaying >= playlistLength){
    queueAdd();
    songPlaying = 0;
  }
  song = minim.loadFile("music/" + songQueue.get(songPlaying).getName(), 2048);
  println("Loaded!");
  song.play();
  framesPlayed = 0;
}

public void skipSong() {
  song.skip(song.length());
  songPlaying ++;
  playSong();
}

public void rewindSong() {
  song.skip(song.length());
  playSong();
}
  public void settings() {  fullScreen(P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "coronaEQ" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
