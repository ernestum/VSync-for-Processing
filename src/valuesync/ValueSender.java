package valuesync;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.serial.Serial;

public class ValueSender {
	private static final char DELIMITER = '|';
	private static final char MESSAGE_END = '#';
	private static final String ALL_VALUES = "A";

	private Serial serial;
	private PApplet parent;
	private long lastValueSent;
	
	private ArrayList<String> observedVariables = new ArrayList<String>();
	private ArrayList<Integer> previousValues = new ArrayList<Integer>();

	public ValueSender(PApplet parent, Serial serial) 
	{
		this.parent = parent;
		this.serial = serial;
		

		parent.registerMethod("pre", this);
	}

	public void pre() 
	{
		syncValues();
	}

	/**
	 * Adds a new values to be synced between the two arduinos
	 */
	public ValueSender observe(String value) 
	{
		observedVariables.add(value);
		previousValues.add(getValue(observedVariables.size()-1) + 1);
		return this;
	}

	/**
	 * Sends all values that have changed since the last call to syncValues
	 */
	void syncValues() 
	{
		int numValuesChanged = 0;
		for(int i = 0; i < observedVariables.size(); i++) 
		{
			if (valueChanged(i))
				numValuesChanged++;
		}
		
		if(numValuesChanged == 0)
			return; //TODO: maybe check for keepalives that need to be sent?
		

		if (allValuesMinPackageSize() < numValuesChanged * singleValueMinPackageSize())
		{
			sendAllValues();
		} 
		else 
		{
			boolean firstValueSent = false;
			for(int i = 0; i < observedVariables.size(); i++) 
			{
				if (valueChanged(i))
				{
					if(firstValueSent)
						serial.write(DELIMITER);
					sendValue(i);
					firstValueSent = true;
				}
			}
			serial.write(MESSAGE_END);
		}

		for(int i = 0; i < observedVariables.size(); i++) 
			previousValues.set(i, getValue(i));
		lastValueSent = System.currentTimeMillis();
	}

	boolean valueChanged(int valueIndex) 
	{
		return getValue(valueIndex) != previousValues.get(valueIndex);
	}

	/**
	 * Sends the x'st value you added to the sender. Returns true if sending was
	 * successfull.
	 */
	boolean sendValue(int index) {
		if (!(index < observedVariables.size() && index >= 0))
			return false;

		serial.write(index + "");
		serial.write(DELIMITER);
		serial.write(getValue(index) + "");

		return true;
	}

//	void sendKeepalive() {
//		serial.write(MESSAGE_END);
//		lastValueSent = System.currentTimeMillis();
//	}

	/**
	 * Sends all values that have been added
	 */
	void sendAllValues() {
		serial.write(ALL_VALUES);
		for (int i = 0; i < observedVariables.size(); i++) {
			serial.write(DELIMITER);
			serial.write(getValue(i) + "");
		}
		serial.write(MESSAGE_END);
	}

	int allValuesMinPackageSize() {
		return 2 + 2 * observedVariables.size();
	}

	int singleValueMinPackageSize() {
		return 4;
	}

	long timeSinceLastMessage() {
		return System.currentTimeMillis() - lastValueSent;
	}
	
	private int getValue(int id)
	{
		return getValue(observedVariables.get(id));
	}

	private int getValue(String value) {
		try {
			return parent.getClass().getField(value).getInt(parent);
		} catch (IllegalArgumentException e) {
			System.err
					.printf("%s is not an integer variable in you sketch!\n Try declaring it as 'int %s;'",
							value, value);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err
					.printf("%s is not acessible variable in you sketch! Try declaring it as public.\n",
							value);
		} catch (NoSuchFieldException e) {
			System.err.printf("%s is not an variable in you sketch!\n", value);
		}
		return -1;
	}
}
