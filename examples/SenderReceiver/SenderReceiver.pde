/**
* To be used together with valueDoubler
*/


import valuesync.*;
import processing.serial.*;

ValueReceiver receiver;
ValueSender sender;


public int doubleMouseX, doubleMouseY;
void setup() {
  size(400,400);
  smooth();
  Serial serial = new Serial(this, "/dev/ttyACM0", 19200);
  
  receiver = new ValueReceiver(this, serial).observe("doubleMouseX").observe("doubleMouseY");
  sender = new ValueSender(this, serial).observe("mouseX").observe("mouseY");
}

void draw() 
{
  println(doubleMouseX);
  noStroke();
  fill(0, 10);
  rect(0, 0, width, height);
  fill(255);
  ellipseMode(CENTER);
  ellipse(doubleMouseX, doubleMouseY, 10, 10);
  ellipse(doubleMouseX/2, doubleMouseY/2, 10, 10);
  
  fill(255, 0, 0);
  ellipse(mouseX, mouseY, 5, 5);
}
