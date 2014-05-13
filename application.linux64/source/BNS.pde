import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

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

void setup () {
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

boolean[] randomBools(int len) {
  boolean[] arr = new boolean[len];

  for (int i = 0; i < len; i++) {
    arr[i] = randomge.nextBoolean();
  }
  return arr;
}

void setAgents() {
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

void draw () {
  fill(0, 80); // fancy fade rendering (transparent background)
  rect(0, 0, gameWidth, height); // draw transparent background
  rect(gameWidth, 0, gameWidth+4, height); // draw transparent background
  fill(255);

  updateAgents();
  updateUI();
}

void updateAgents() {
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

void updateUI() {
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

void keyPressed() {
  if (key == 'P' || key == 'p') {
    penalty = !penalty;
  }
  if (key == 'F' || key == 'f') {
    friendmode = !friendmode;
  }
}

