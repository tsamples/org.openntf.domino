package org.openntf.domino.tests.rpr.formula;

import java.text.SimpleDateFormat;

import org.openntf.domino.DateTime;
import org.openntf.domino.tests.rpr.formula.eval.Formatter;

public class DominoFormatter implements Formatter {

	public DateTime parseDate(final String image) throws java.text.ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

		//Date d = formatter.parse(image);
		DateTime dt = new org.openntf.domino.impl.DateTime();
		System.out.println("Image: " + image);
		dt.setLocalTime(image);
		//dt.setAnyTime();
		return dt;

	}

	public Number parseNumber(final String el) {
		//NumberFormat nf = NumberFormat.getInstance();
		// TODO Auto-generated method stub
		return Double.valueOf(el.replace(',', '.'));
	}
}
