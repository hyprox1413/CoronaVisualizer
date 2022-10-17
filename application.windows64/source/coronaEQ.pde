import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.effects.*;
import ddf.minim.signals.*;
import ddf.minim.spi.*;
import ddf.minim.ugens.*;
import processing.video.*;

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

  void update() {
    for (int i = history.length - 1; i > 0; i --) {
      history[i] = history[i - 1];
    }
  }

  float val() {
    float total = 0;
    for (int i = 0; i < history.length; i ++) {
      total += history[i];
    }
    return total / history.length;
  }
}

void checkSongOver() {
  framesPlayed ++;
  //println(framesPlayed + " " + song.duration() * 60);
  if (!song.isPlaying()) {
    skipSong();
  }
}

void mouseClicked() {
  if (mouseButton == LEFT) {
    skipSong();
  } else if (mouseButton == RIGHT) {
    rewindSong();
  }
}

void setup() {
  fullScreen(P2D);
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

void draw() {
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

void dots() {
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

void corona(float x, float y, float r, float theta, boolean o) {
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
