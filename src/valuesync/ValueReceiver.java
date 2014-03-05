/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package valuesync;


import java.util.ArrayList;

import processing.core.PApplet;
import processing.serial.Serial;


/**
 * This is a template class and can be used to start a new processing library or tool.
 * Make sure you rename this class as well as the name of the example package 'template' 
 * to your own library or tool naming convention.
 * 
 * @example Hello 
 * 
 * (the tag @example followed by the name of an example included in folder 'examples' will
 * automatically include the example in the javadoc.)
 *
 */

public class ValueReceiver 
{
	private StringBuffer messageBuffer = new StringBuffer();
	private Serial serial;
	private PApplet parent;
	private ArrayList<String> observedVariables = new ArrayList<String>();
	
	public final static String VERSION = "##library.prettyVersion##";
	


	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param parent
	 */
	public ValueReceiver(PApplet parent, Serial serial) 
	{
		this.parent = parent;
		this.serial = serial;
		parent.registerMethod("pre", this);
	}
	
	public ValueReceiver observe(String variable)
	{
		observedVariables.add(variable);
		return this;
	}
	
	public void pre()
	{
		while(serial.available() > 0)
		{
			char in = serial.readChar();
//			PApplet.print(in);
			if(in == '#')
			{
				analyzeMessage(messageBuffer.toString());
				messageBuffer = new StringBuffer();
			}
			else
			{
				messageBuffer.append(in);
			}
			
		}
	}
	
	private void analyzeMessage(String s)
	{
		if(s == null || s.length() == 0) return;
		String[] items = s.split("\\|");
		if(items.length == 0) return;
		
		try
		{
			if(items[0].charAt(0) == 'A')
			{
				if(items.length != observedVariables.size() + 1)
				{
					return;
				}
				for(int i = 0; i < observedVariables.size(); i++)
				{
					setValue(observedVariables.get(i), Integer.parseInt(items[i+1]));
				}
			}
			else
			{
				if(items.length % 2 != 0) return;
				
				for(int i = 0; i < items.length / 2; i++)
				{
					int index = Integer.parseInt(items[i*2]);
					int value = Integer.parseInt(items[i*2 + 1]);
					setValue(observedVariables.get(index), value);
				}
			}
		}
		catch (NumberFormatException e)
		{}
		
	}
	
	private void setValue(String valueName, int value)
	{
		try {
			parent.getClass().getField(valueName).setInt(parent, value);
		} catch (IllegalArgumentException e) {
			System.err.printf("%s is not an integer variable in you sketch!\n Try declaring it as 'int %s;'", valueName, valueName);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.printf("%s is not acessible variable in you sketch! Try declaring it as public.\n", valueName);
		} catch (NoSuchFieldException e) {
			System.err.printf("%s is not a variable in you sketch!\n", valueName);
		}
	}
	
	
	//TODO: ist das kunst oder kann das weg? vvv
	
	private void welcome() 
	{
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}
	
	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	public void setSerial(Serial serial)
	{
		serial.clear();
		this.serial = serial;
	}
	
	public void stopSerial()
	{
		serial.stop();
	}
}

