
import valuesync.*;
import processing.serial.*;

ValueReceiver receiver;

public int analogValue;

void setup() {
  size(400,400);

  Serial serial = new Serial(this, "/dev/ttyUSB0", 19200);
  
  receiver = new ValueReceiver(this, serial).observe("analogValue");
  
}

void draw() 
{
  println(analogValue);
  background(analogValue/4);
}
