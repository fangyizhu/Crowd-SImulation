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

  void update(float elapsedTime) {
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
        desired.mult(-0.5);
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

  void draw() {
    
    ellipse(position.x, position.y, 6, 6);
  }
}

