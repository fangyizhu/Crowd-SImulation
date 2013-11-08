import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.List; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class HW4 extends PApplet {



int agentsNum = 300;
int obstacleNum = 10;
List<Agent> agents;
List<Obstacle> obstacles;
boolean followMouse;

public void setup () {
  noStroke();
  size(1024, 760, P2D);
  agents = new ArrayList<Agent>();
  obstacles = new ArrayList<Obstacle>();
  followMouse = false;
  background(0);
  for (int i = 0; i < agentsNum; i++) {
    Agent agent = new Agent(new PVector(random(20, width-20), random(20, height-20)), // position
    new PVector(random(-20, 20), random(-20, 20), 0)); // velocity
    agents.add(agent);
  }
  for (int i = 0; i < obstacleNum; i++) {
    Obstacle obstacle = new Obstacle(
    new PVector(random(50, width-50), random(50, height-50)), //position
    random(30, 100), //size
    (int)random(0, 255), (int)random(0, 255), (int)random(0, 255)); //color
    obstacles.add(obstacle);
  }
}

public void draw () {
  background(0);
  float elapsedTime = constrain(1f/frameRate, 16f/1000f, 32f/1000f);
  fill(255);
  for (Agent agent : agents) {
    agent.update(elapsedTime);
    agent.draw();
  }

  for (Obstacle obstacle : obstacles) {
    fill(obstacle.r, obstacle.g, obstacle.b);
    obstacle.draw();
  }
}

public void mouseClicked() {
  followMouse = !followMouse;
}

float cohesion = 5f;
float alignment = 0.05f;
float separation = 6.5f;
float separationDistMinSq = 36f;
float separationDistSq = 285f;
float interactDistSq = 325f;

class Agent {
  PVector position;
  PVector velocity;

  Agent(PVector pos, PVector vel) {
    position = pos.get();
    velocity = vel.get();
  }

  public void update(float elapsedTime) {
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

    for (Obstacle obstacle : obstacles) {
      PVector obsPos = obstacle.position;

      float xDiff = position.x - obsPos.x;
      float yDiff = position.y - obsPos.y;
      float distSq = xDiff * xDiff + yDiff * yDiff;

      if (distSq < obstacle.oSeparationDistSq) {
        separationSum.add(obsPos);
        separationCount++;
      }
    }


    PVector acceleration;
    if (followMouse) {
      acceleration = new PVector((mouseX - position.x) / 70, (mouseY - position.y) / 70);
    }
    else {
      acceleration = new PVector(0, 0);
    }

    if (cohesionCount > 0) {
      cohesionSum.div(cohesionCount);
      PVector desired = PVector.sub(position, cohesionSum);
      desired.mult(cohesion);
      if (cohesionCount > 10 && cohesion > 0) {
        desired.mult(-0.5f);
      }
      acceleration.sub(desired);
    }

    if (alignmentCount > 0) {
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

    if (position.x > (width - 20))
      velocity.x *= -1;
    if (position.x < 20)
      velocity.x *= -1;
    if (position.y > (height - 20))
      velocity.y *= -1;
    if (position.y < 20)
      velocity.y *= -1;

    float velMagSq = velocity.x * velocity.x + velocity.y * velocity.y;
    if (velMagSq > 6400) {
      float velMag = sqrt(velMagSq);
      velocity.x = (velocity.x / velMag) * 80;
      velocity.y = (velocity.y / velMag) * 80;
    }

    position.add(PVector.mult(velocity, elapsedTime));
  }

  public void draw() {
    
    ellipse(position.x, position.y, 6, 6);
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
    String[] appletArgs = new String[] { "HW4" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
