package valuesync.library;

import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PApplet;
import processing.serial.Serial;

public class ValueSender {
	private static final char DELIMITER = '|';
	private static final char MESSAGE_START = '|';
	private static final String ALL_VALUES = "-1";

	private Serial serial;
	private PApplet parent;
	private ArrayList<String> values;
	private long lastValueSent;

	private HashMap<String, Integer> previousValues = new HashMap<String, Integer>();

	public ValueSender(PApplet parent, Serial serial, String[] values) {
		this.parent = parent;
		this.serial = serial;
		this.values = new ArrayList<String>(values.length);

		for (String value : values)
			this.values.add(value);

		parent.registerMethod("pre", this);
	}

	public void pre() {

	}

	/**
	 * Adds a new values to be synced between the two arduinos
	 */
	ValueSender addValue(String value) {
		values.add(value);
		return this;
	}

	/**
	 * Sends all values that have changed since the last call to syncValues
	 */
	void syncValues() {
		int numValuesChanged = 0;
		for (String value : values) {
			if (valueChanged(value))
				numValuesChanged++;
		}

		if (allValuesMinPackageSize() < numValuesChanged
				* singleValueMinPackageSize()) {
			sendAllValues();

		} else {
			for (String value : values) {
				if (getValue(value) != previousValues.get(value))
					sendValue(value);
			}
		}

		for (String value : values)
			previousValues.put(value, getValue(value));
	}

	boolean valueChanged(String value) {
		return getValue(value) != previousValues.get(value);
	}

	/**
	 * Sends a value. You need to add it firtst via addValue.
	 */
	boolean sendValue(String value) {
		return sendValue(indexForValue(value));
	}

	int indexForValue(String value) {
		return values.indexOf(value);
	}

	/**
	 * Sends the x'st value you added to the sender. Returns true if sending was
	 * successfull.
	 */
	boolean sendValue(int index) {
		if (!(index < values.size() && index >= 0))
			return false;

		serial.write(MESSAGE_START);
		serial.write(index + "");
		serial.write(DELIMITER);
		serial.write(getValue(values.get(index)) + "");
		serial.write(DELIMITER);

		lastValueSent = System.currentTimeMillis();

		return true;
	}

	void sendKeepalive() {
		serial.write(MESSAGE_START);
		lastValueSent = System.currentTimeMillis();
	}

	/**
	 * Sends all values that have been added
	 */
	void sendAllValues() {
		serial.write(MESSAGE_START);
		serial.write(ALL_VALUES);
		serial.write(DELIMITER);
		for (int i = 0; i < values.size(); i++) {
			serial.write(getValue(values.get(i)) + "");
			serial.write(DELIMITER);
		}

		lastValueSent = System.currentTimeMillis();
	}

	int allValuesMinPackageSize() {
		return 2 + 2 * values.size();
	}

	int singleValueMinPackageSize() {
		return 5;
	}

	long timeSinceLastMessage() {
		return System.currentTimeMillis() - lastValueSent;
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
