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
  
  void draw() {
    ellipse(position.x, position.y, radius, radius);
  }
}

