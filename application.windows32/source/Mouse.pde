class Mouse {
  PVector position;
  float oSeparationDistSq = 100;

  Mouse() {
    position = new PVector(mouseX, mouseY);
  }
  
  void update() {
    position = new PVector(mouseX, mouseY);
  }
}
