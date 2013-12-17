import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.List; 
import java.util.Timer; 
import java.util.TimerTask; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class BoidGame extends PApplet {





int agentsNum = 800;
int obstacleNum = 15;
int gameWidth;
int infoWid = 200;
List<Agent> agents;
float objx;
float objy;
int objSize = 50;
float speed;
Agent agent;
Timer timer1;
Timer timer2;
Timer timer3;
TimerTask task1;
TimerTask task2;
TimerTask task3;
TimerTask task4;
boolean die; // 1 = playing, 2 = die, 3 = win

int count;
int purge;

public void setup () {
  speed = 3.2f;
  gameWidth = 800;
  objx = gameWidth/2 ;
  objy = height/2;
  die = false;
  purge = 0;

  frame.setTitle("EAT THEM ALL!!!");
  textSize(20);
  smooth();
  noStroke();
  background(0);
  size(gameWidth+infoWid, 800, P2D);

  setAgents();
  setTimers();
}

public void setAgents() {
  agents = new ArrayList<Agent>();
  for (int i = 0; i < agentsNum; i++) {
    agent = new Agent(new PVector(random(20, gameWidth-20), random(20, height-20)), // position
    new PVector(random(-20, 20), random(-20, 20), 0)); // velocity
    agents.add(agent);
  }
}

public void setTimers() {
  /*--- set evolvement timer ---*/
  class Task1 extends TimerTask { //affected
    public void run() {
      agents.get((int)random(0, agents.size())).evolve(1);
    }
  }
  task1 = new Task1();

  class Task2 extends TimerTask { //speed
    public void run() {
      agents.get((int)random(0, agents.size())).evolve(2);
    }
  }
  task2 = new Task2();

  class Task3 extends TimerTask { //stop generating speed
    public void run() {
      timer2.cancel();
    }
  }
  task3 = new Task3();

  class Task4 extends TimerTask { //stop generating speed
    public void run() {
      agents.get((int)random(0, agents.size())).evolve(3);
    }
  }
  task4 = new Task4();

  timer1 = new Timer();
  timer2 = new Timer();
  timer3 = new Timer();

  timer1.schedule(task1, 100000, 10000);

  timer2.schedule(task2, 0, 20000);
  timer2.schedule(task3, 300000);

  timer3.schedule(task4, 200000, 30000);
}

public void draw () {
  fill(0, 80); // fancy fade rendering (transparent background)
  rect(0, 0, gameWidth, height); // draw transparent background
  rect(gameWidth, 0, gameWidth+4, height); // draw transparent background
  keyBoard();
  fill(255);
  if (agents.size() == 0) {
    text("You win!", gameWidth / 2, 150);
    timer1.cancel();
    timer2.cancel();
    timer3.cancel();
  }
  else if (die) {
    text("You are dead!", gameWidth / 2, 150);
  } 
  else {
    drawModeObjects();
    updateAgents();
    updateUI();
  }
}

public void updateAgents() {
  float elapsedTime = constrain(1f/frameRate, 16f/1000f, 32f/1000f);
  int i = 0;
  while (i<agents.size () && agents.size()!=0) {
    Agent agent = agents.get(i);
    if (agent.species == 1 && purge > 0) {
      agent.evolve(0);
      purge--;
    }
    if ((agent.position.x > objx) && (agent.position.y > objy) && (agent.position.x < (objx+objSize)) && (agent.position.y < (objy+objSize))) {
      if (agent.species == 2) {
        speed += 0.1f;
      } 
      else if (agent.species == 1) {
        die = true;
      }
      else if (agent.species == 3) {
        purge += 4;
      }
      agents.remove(i);
    } 
    else {
      agent.update(elapsedTime);
      agent.draw();
      i++;
    }
  }
}

public void updateUI() {
  text("Eat them all!!!", gameWidth+30, 50); 
  text("left:", gameWidth+30, 100);
  text(count, gameWidth+100, 100);
  count = agents.size();
  text(speed, gameWidth+100, 150);
  text("speed", gameWidth+30, 150);

  fill(255);
  ellipse(gameWidth+30, 300, 4, 4);
  text("normal", gameWidth+50, 300);

  fill(255, 0, 0);
  ellipse(gameWidth+30, 350, 4, 4);
  text("infected killer", gameWidth+50, 350);

  fill(0, 255, 0);
  ellipse(gameWidth+30, 400, 4, 4);
  text("speed up", gameWidth+50, 400);

  fill(0, 0, 255);
  ellipse(gameWidth+30, 450, 4, 4);
  text("killer purifier", gameWidth+50, 450);
}

public void drawModeObjects() {
  fill(0, 0, 255);
  rect(objx, objy, objSize, objSize);
}

public void keyBoard() {
  if (keyPressed && key==CODED) {
    switch(keyCode) {
    case UP: 
      if (objy >= 0) {
        objy-=speed;
      }
      break;
    case DOWN:
      if (objy <= height - objSize) {
        objy+=speed;
      }
      break;
    case RIGHT:
      if (objx <= gameWidth - objSize) {
        objx+=speed;
      }
      break;
    case LEFT:
      if (objx >= 0) {
        objx-=speed;
      }
      break;
    }
  }
}

float cohesion = 5f;
float alignment = 1f;
float separation = 7f;
float separationDistMinSq = 36f;
float separationDistSq = 285f;
float interactDistSq = 325f;

class Agent {
  PVector position;
  PVector velocity;
  int species; // 0 = normal, 1 = affected, 2 = speed, 3 = purify

  Agent(PVector pos, PVector vel) {
    position = pos.get();
    velocity = vel.get();
    species = 0;
  }

  public void evolve(int species) {
    this.species = species;
  }

  public void update(float elapsedTime) {
    /*-- edge --*/
    if (species == 0) {
      if (position.x > gameWidth)
        position.x = 2;
      if (position.x < -1)
        position.x = gameWidth;
      if (position.y > height)
        position.y = 1;
      if (position.y < -1)
        position.y = height;
    }

    PVector cohesionSum = new PVector(0, 0);
    int cohesionCount = 0;

    PVector alignmentSum = new PVector(0, 0);
    int alignmentCount = 0;

    PVector separationSum = new PVector(0, 0);
    int separationCount = 0;

    for (Agent agent : agents) {
      PVector otherPos = agent.position;

      float xDiff = position.x - otherPos.x;
      float yDiff = position.y - otherPos.y;
      float distSq = xDiff * xDiff + yDiff * yDiff;

      if (agent != this && distSq < interactDistSq) {
        cohesionSum.add(agent.position);
        cohesionCount++;

        alignmentSum.add(agent.velocity);
        alignmentCount++;

        if (distSq < separationDistSq) {
          separationSum.add(otherPos);
          separationCount++;
          if (distSq < separationDistMinSq) { 
            separationSum.add(PVector.mult(otherPos, 12));
            separationCount += 12;
          }
        }
      }
    }

    PVector acceleration;
    acceleration = new PVector(0, 0);

    if (cohesionCount > 0 && species != 1) {
      cohesionSum.div(cohesionCount);
      PVector desired = PVector.sub(position, cohesionSum);
      desired.mult(cohesion);
      if (cohesionCount > 10 && cohesion > 0) {
        desired.mult(-0.25f);
      }
      acceleration.sub(desired);
    }

    if (alignmentCount > 0 && species != 1) {
      alignmentSum.div(alignmentCount);
      alignmentSum.mult(alignment);
      acceleration.add(alignmentSum);
    }

    if (separationCount > 0) {
      separationSum.div(separationCount);
      PVector desired = PVector.sub(position, separationSum);
      desired.mult(separation);
      acceleration.add(desired);
    }

    /*--- Scared by Box ---*/
    if (species != 1) {
      float oDiffX = objx - position.x; 
      float oDiffY = objy - position.y;
      float oDistSq = oDiffX * oDiffX + oDiffY * oDiffY;
      if (oDistSq < 10000) {
        float oDist = sqrt(oDistSq);
        acceleration.sub((oDiffX/oDist) * 100, (oDiffY/oDist) * 100, 0);
      }
    }

    velocity.add(acceleration);

    float velMagSq = velocity.x * velocity.x + velocity.y * velocity.y;
    if (velMagSq > 6400) {
      float velMag = sqrt(velMagSq);
      velocity.x = (velocity.x / velMag) * 80;
      velocity.y = (velocity.y / velMag) * 80;
    }

    position.add(PVector.mult(velocity, elapsedTime));

    if (species != 0) {
      if (position.x > gameWidth)
        position.x = gameWidth;
      if (position.x < -1)
        position.x = 2;
      if (position.y > height)
        position.y = height - 1;
      if (position.y < -1)
        position.y = 1;
    }
  }

  public void draw() {
    switch (species) {
    case 1:  
      fill(255, 0, 0);
      ellipse(position.x, position.y, 3, 3);
      break;
    case 2:  
      fill(0, 255, 0);
      ellipse(position.x, position.y, 3, 3);
      break;
    case 3:  
      fill(0, 0, 255);
      ellipse(position.x, position.y, 3, 3);
      break;
    default: 
      fill(255);
      ellipse(position.x, position.y, 2, 2);
      break;
    }
  }
}

class Mouse {
  PVector position;
  float oSeparationDistSq = 100;

  Mouse() {
    position = new PVector(mouseX, mouseY);
  }
  
  public void update() {
    position = new PVector(mouseX, mouseY);
  }
}
class Obstacle {
  PVector position;
  float radius;
  float oSeparationDistSq;
  int r;
  int g;
  int b;

  Obstacle(PVector pos, float radius, int r, int g, int b) {
    position = pos.get();
    this.radius = radius;
    oSeparationDistSq = (radius - 10) * (radius - 10);
    this.r = r;
    this.g = g;
    this.b = b;
  }
  
  public void draw() {
    ellipse(position.x, position.y, radius, radius);
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "BoidGame" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
