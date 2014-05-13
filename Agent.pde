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

  void update(float elapsedTime) {
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
        desired.mult(-0.25);
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

  void draw() {
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

