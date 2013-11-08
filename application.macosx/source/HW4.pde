import java.util.List;

int agentsNum = 300;
int obstacleNum = 10;
List<Agent> agents;
List<Obstacle> obstacles;
boolean followMouse;

void setup () {
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

void draw () {
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

void mouseClicked() {
  followMouse = !followMouse;
}

