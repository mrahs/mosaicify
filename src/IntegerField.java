/*
	Copyright 2013 Anas H. Sulaiman (ahs.pw)
	
	This file is part of Mosaicify.

    Mosaicify is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Mosaicify is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Mosaicify.  If not, see <http://www.gnu.org/licenses/>.
*/

package pw.ahs.mosaicify;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class IntegerField extends TextField {
	final private IntegerProperty value;
	final private int minValue;
	final private int maxValue;

	// expose an integer value property for the text field.
	public int getValue() {
		return value.getValue();
	}

	public void setValue(int newValue) {
		value.setValue(newValue);
	}

	public IntegerProperty valueProperty() {
		return value;
	}

	IntegerField(int minValue, int maxValue, int initialValue) {
		if (minValue > maxValue)
			throw new IllegalArgumentException(
					"min value must be greater than max value");
		if (!((minValue <= initialValue) && (initialValue <= maxValue)))
			throw new IllegalArgumentException(
					"initial value must be in range specified by min and max");
		// initialize the field values.
		this.minValue = minValue;
		this.maxValue = maxValue;
		value = new SimpleIntegerProperty(initialValue);
		setText(initialValue + "");
		final IntegerField intField = this;

		// make sure the value property is clamped to the required range
		// and update the field's text to be in sync with the value.
		value.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldValue, Number newValue) {
				if (newValue == null) {
					intField.setText("");
				} else {
					if (newValue.intValue() < intField.minValue) {
						value.setValue(intField.minValue);
						return;
					}
					if (newValue.intValue() > intField.maxValue) {
						value.setValue(intField.maxValue);
						return;
					}

					intField.setText(newValue.toString());
				}
			}
		});
		// restrict key input to numerals.
		this.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (!"0123456789".contains(keyEvent.getCharacter())) {
					keyEvent.consume();
				}
			}
		});
		// update value
		this.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {

				if (newValue.booleanValue() == true)
					return;

				updateValue();
			}
		});
		this.addEventFilter(KeyEvent.KEY_RELEASED,
				new EventHandler<KeyEvent>() {

					@Override
					public void handle(KeyEvent event) {
						if (event.getCode() != KeyCode.ENTER)
							return;

						updateValue();
					}
				});
	}

	private void updateValue() {
		String txtValue = textProperty().get();
		if (txtValue.isEmpty()) {
			value.set(0);
			return;
		}
		try {
			value.set(Integer.parseInt(txtValue));
		} catch (NumberFormatException e) {
			value.set(0);
		}
	}
}
