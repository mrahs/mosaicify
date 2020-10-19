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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class DoubleField extends TextField {

	final private DoubleProperty value;
	final private double minValue;
	final private double maxValue;

	// expose an integer value property for the text field.
	public double getValue() {
		return value.getValue();
	}

	public void setValue(double newValue) {
		value.setValue(newValue);
	}

	public DoubleProperty valueProperty() {
		return value;
	}

	DoubleField(double minValue, double maxValue, double initialValue) {
		if (minValue > maxValue)
			throw new IllegalArgumentException(
					"min value must be greater than max value");
		if (!((minValue <= initialValue) && (initialValue <= maxValue)))
			throw new IllegalArgumentException(
					"initial value must be in range specified by min and max");
		// initialize the field values.
		this.minValue = minValue;
		this.maxValue = maxValue;
		value = new SimpleDoubleProperty(initialValue);
		setText(initialValue + "");
		final DoubleField dblField = this;

		// make sure the value property is clamped to the required range
		// and update the field's text to be in sync with the value.
		value.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldValue, Number newValue) {
				if (newValue == null) {
					dblField.setText("");
				} else {
					if (newValue.doubleValue() < dblField.minValue) {
						value.setValue(dblField.minValue);
						return;
					}
					if (newValue.doubleValue() > dblField.maxValue) {
						value.setValue(dblField.maxValue);
						return;
					}

					dblField.setText(newValue.toString());
				}
			}
		});
		// restrict key input to numerals.
		this.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (keyEvent.getCharacter().equals(".")
						&& dblField.getText().contains("."))
					keyEvent.consume();
				else if (!"0123456789.".contains(keyEvent.getCharacter())) {
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
			value.set(0.0);
			return;
		}
		try {
			value.set(Double.parseDouble(txtValue));
		} catch (NumberFormatException e) {
			value.set(0.0);
		}
	}
}
