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

package valuesync.library;


import java.util.ArrayList;
import java.util.Scanner;

import processing.core.*;
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

public class ValueReceiver {
	
	private Serial serial;
	private PApplet parent;
	private ArrayList<String> values;

	
	public final static String VERSION = "##library.prettyVersion##";
	

	public ValueReceiver(PApplet parent, Serial serial)
	{
		this(parent, serial, new String[] {});
	}
	
	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param parent
	 */
	public ValueReceiver(PApplet parent, Serial serial, String[] values) {
		this.parent = parent;
		this.serial = serial;
		this.values = new ArrayList<String>(values.length);
		
		for(String value : values)
			this.values.add(value);
		
		serial.readStringUntil('#');
		
		parent.registerMethod("pre", this);
	}
	
	public void pre()
	{
		for(String valuePacket = serial.readStringUntil('#'); valuePacket != null; valuePacket = serial.readStringUntil('#'))
		{
			if("#".equals(valuePacket)) //Just a keepalive
				continue;
			
			processValuePacket(valuePacket);
		}
	}
	
	private void processValuePacket(String valuePacket)
	{
		Scanner s = new Scanner(valuePacket);
		s.useDelimiter("\\|");
		int valueIndex = s.nextInt();
		if(valueIndex == -1)
		{
			for(int i = 0; i < values.size(); i++)
				setValue(values.get(i), s.nextInt());
		}
		else
		{
			if(valueIndex >=0 && valueIndex < values.size())
			{
				int value = s.nextInt();
				setValue(values.get(valueIndex), value);
			}
			else
				System.err.printf("Warning: we received a %dth value but you only delcared %d values. Something is missconfigured here!\n", valueIndex + 1, values.size());
		}
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
			System.err.printf("%s is not an variable in you sketch!\n", valueName);
		}
	}
	

	
	private void welcome() {
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

}

