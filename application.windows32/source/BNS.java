import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.List; 
import java.util.Timer; 
import java.util.TimerTask; 
import java.util.Random; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class BNS extends PApplet {






int agentsNum = 900;
int gameWidth;
int infoWid = 250;
List<Agent> agents;
Agent agent;
Random randomge = new Random();
int agentSize;

int count;

int geneLen = 6;
boolean breeded;
Agent newAgent = new Agent();

boolean friendmode;
boolean penalty;

public void setup () {
  gameWidth = 800;

  frame.setTitle("Natural Selection of Boids");
  textSize(15);
  smooth();
  noStroke();
  background(0);
  size(gameWidth+infoWid, 800, P2D);

  setAgents();

  breeded = false;
  friendmode = true;
  penalty = true;
}

public boolean[] randomBools(int len) {
  boolean[] arr = new boolean[len];

  for (int i = 0; i < len; i++) {
    arr[i] = randomge.nextBoolean();
  }
  return arr;
}

public void setAgents() {
  agents = new ArrayList<Agent>();
  for (int i = 0; i < agentsNum; i++) {
    int species = i%4;
    PVector position = new PVector(random(20, gameWidth-20), random(20, height -20));
    position.div(2);
    switch (species) {
    case 1:  
      break;
    case 2:  
      position.y += height/2;
      break;
    case 3:  
      position.x += gameWidth/2;
      break;
    default: 
      position.add(new PVector(gameWidth/2, height/2));
      break;
    }

    agent = new Agent(position, // position
    new PVector(0, 0), 
    species, 
    randomBools(geneLen)); //species

    agents.add(agent);
  }
}

public void draw () {
  fill(0, 80); // fancy fade rendering (transparent background)
  rect(0, 0, gameWidth, height); // draw transparent background
  rect(gameWidth, 0, gameWidth+4, height); // draw transparent background
  fill(255);

  updateAgents();
  updateUI();
}

public void updateAgents() {
  float elapsedTime = constrain(1f/frameRate, 16f/1000f, 32f/1000f);
  int i = 0;
  agentSize = agents.size();
  while (i<agents.size () && agentSize!=0) {
    Agent agent = agents.get(i);
    agent.update(elapsedTime);

    //breed
    if (breeded) {
      agents.add(newAgent);
      breeded = false;
    } 

    agent.draw();
    if (agent.life <= 0) {
      agents.remove(i);
    }
    i++;
  }
}

public void updateUI() {
  fill(255);
  text("Natural Selection of Boids", gameWidth+30, 50); 
  text("left:", gameWidth+30, 100);
  text(agentSize, gameWidth+100, 100);

  if (penalty && agentSize > 1000) {
    text("Population Penalty", gameWidth + 30, 150);
  }

  if (penalty && agentSize > 1200) {
    text("Population Penalty 2", gameWidth + 30, 200);
  }

  text("Population Penalty:", gameWidth+30, 250); 
  if (penalty) {
    text("On (Press 'p' to toggle)", gameWidth + 30, 300);
  }
  else {
    text("Off (Press 'p' to toggle)", gameWidth + 30, 300);
  }
  
  text("Same Color is:", gameWidth+30, 450); 
  if (friendmode) {
    text("Friend (Press 'f' to toggle)", gameWidth + 30, 500);
  }
  else {
    text("Enemy (Press 'f' to toggle)", gameWidth + 30, 500);
  }
}

public void keyPressed() {
  if (key == 'P' || key == 'p') {
    penalty = !penalty;
  }
  if (key == 'F' || key == 'f') {
    friendmode = !friendmode;
  }
}

float cohesion = 5f;
float alignment = 1f;
float separation = 7f;
float separationDistMinSq = 100f;
float separationDistSq = 285f;
float interactDistSq = 325f;

class Agent {
  PVector position;
  PVector velocity;
  int species; // 0 = red, 1 = green, 2 = blue, 3 = white
  boolean[] gene;
  // f cohesion | f alignment | f separation | e cohesion | e alignment | e separation
  int life;
  int breed;
  int mature;

  Agent() {
  }

  Agent(PVector pos, PVector vel, int spe, boolean[] ge) {
    position = pos.get();
    velocity = vel.get();
    species = spe;
    gene = ge;
    life = 10000;
    breed = 300;
    mature = 2000;
  }

  public void update(float elapsedTime) {
    life--;

    mature--;

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

      if (distSq < interactDistSq && agent != this) {
        if (friendmode) { //same color is friend
          if (species == agent.species) {
            if (!penalty || agentSize < 1000) { 
              life++;
            } 
            else if (agentSize >= 1200) { // population penalty
              if (randomge.nextBoolean()) {
                life--;
              }
            }
          }  
          else {
            life--;
          }
        } 
        else { // same color is enemy
          if (species != agent.species) {
            if (!penalty || agentSize < 1000) { // population penalty
              life++;
            } 
            else if (agentSize >= 1200) {
              if (randomge.nextBoolean()) { // population penalty
                life--;
              }
            }
          }  
          else {
            life--;
          }
        }

        //breed?
        if (species == agent.species && this.mature < 0 && agent.mature < 0 && !breeded) {
          breeded = true;
          PVector childPos = PVector.add(this.position, agent.position);
          childPos.div(2);

          boolean[] childGene = new boolean[geneLen];
          for (int i = 0; i < geneLen ; i++) {
            if (this.gene[i] == agent.gene[i]) {
              //mutation
              float r = random(0, 100000);
              if (r > 1) {
                childGene[i] = this.gene[i];
              } 
              else {
                childGene[i] = !this.gene[i];
              }
            } 
            else {
              childGene[i] = randomge.nextBoolean();
            }
          }

          newAgent = new Agent(childPos, new PVector(0, 0), this.species, childGene);

          this.mature = 2500;
          this.life -= 100;
          agent.mature = 2500;
          this.life -= 100;
        }

        if ((gene[0] && (species == agent.species)) || (gene[3]) && (species != agent.species)) {
          cohesionSum.add(agent.position);
          cohesionCount++;
        }

        if ((gene[1] && (species == agent.species)) || (gene[4]) && (species != agent.species)) {
          alignmentSum.add(agent.velocity);
          alignmentCount++;
        }

        if ((gene[2] && (species == agent.species)) || (gene[5]) && (species != agent.species)) {
          if (distSq < separationDistSq) {
            separationSum.add(otherPos);
            separationCount++;
          }
        }

        if (distSq < separationDistMinSq) { 
          separationSum.add(PVector.mult(otherPos, 12));
          separationCount += 12;
        }
      }
    }


    PVector acceleration;
    acceleration = new PVector(0, 0);

    if (cohesionCount > 0) {
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

    velocity.add(acceleration);

    float velMagSq = velocity.x * velocity.x + velocity.y * velocity.y;
    if (velMagSq > 6400) {
      float velMag = sqrt(velMagSq);
      velocity.x = (velocity.x / velMag) * 80;
      velocity.y = (velocity.y / velMag) * 80;
    }

    position.add(PVector.mult(velocity, elapsedTime));

    if (position.x > gameWidth)
      position.x = 1;
    if (position.x < -1)
      position.x = gameWidth;
    if (position.y > height)
      position.y = 1;
    if (position.y < -1)
      position.y = height - 1;
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
      fill(255, 255, 255);
      ellipse(position.x, position.y, 3, 3);
      break;
    default: 
      fill(0, 0, 255);
      ellipse(position.x, position.y, 3, 3);
      break;
    }
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "BNS" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
