import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

void setup () {
  speed = 3.2;
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

void setAgents() {
  agents = new ArrayList<Agent>();
  for (int i = 0; i < agentsNum; i++) {
    agent = new Agent(new PVector(random(20, gameWidth-20), random(20, height-20)), // position
    new PVector(random(-20, 20), random(-20, 20), 0)); // velocity
    agents.add(agent);
  }
}

void setTimers() {
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

void draw () {
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

void updateAgents() {
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
        speed += 0.1;
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

void updateUI() {
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

void drawModeObjects() {
  fill(0, 0, 255);
  rect(objx, objy, objSize, objSize);
}

void keyBoard() {
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

