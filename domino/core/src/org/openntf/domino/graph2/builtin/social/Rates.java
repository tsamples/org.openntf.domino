package org.openntf.domino.graph2.builtin.social;

import org.openntf.domino.graph2.annotations.TypedProperty;
import org.openntf.domino.graph2.builtin.DEdgeFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("Rates")
public interface Rates extends DEdgeFrame {
	public static final String LABEL_RATES = "rates";

	@TypedProperty("Rating")
	public int getRating();

	@TypedProperty("Rating")
	public void setRating(int rating);

}