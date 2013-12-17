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

  void evolve(int species) {
    this.species = species;
  }

  void update(float elapsedTime) {
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

