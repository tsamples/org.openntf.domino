/**
 * 
 */
package org.openntf.domino;

import java.util.Collection;
import java.util.Vector;

import org.openntf.domino.types.FactorySchema;

/**
 * @author Roland Praml, Foconis AG
 * 
 */
public interface WrapperFactory {

	/**
	 * Wraps the lotus object in the apropriate wrapper object
	 * 
	 * @param lotus
	 *            the object to wrap
	 * @param schema
	 *            the schema that ensures type safeness
	 * @param parent
	 *            the parent element
	 * @param <T>
	 *            the generic org.openntf.domino type (wrapper)
	 * @param <D>
	 *            the generic lotus.domino type (delegate)
	 * @param <P>
	 *            the generic org.openntf.domino type (parent)
	 * @return the wrapper
	 */
	@SuppressWarnings("rawtypes")
	<T extends Base, D extends lotus.domino.Base, P extends Base> T fromLotus(D lotus, FactorySchema<T, D, P> schema, P parent);

	/**
	 * Wraps a collection of lotus objects in a collection of apropriate wrapper objects
	 * 
	 * @param lotusColl
	 *            the object-collection to wrap
	 * @param schema
	 *            the schema that ensures type safeness
	 * @param parent
	 *            the parent element
	 * @param <T>
	 *            the generic org.openntf.domino type (wrapper)
	 * @param <D>
	 *            the generic lotus.domino type (delegate)
	 * @param <P>
	 *            the generic org.openntf.domino type (parent)
	 * 
	 * @return the wrapper-collection
	 */
	@SuppressWarnings("rawtypes")
	<T extends Base, D extends lotus.domino.Base, P extends Base> Collection<T> fromLotus(Collection<?> lotusColl,
			FactorySchema<T, D, P> schema, P parent);

	/**
	 * Wraps a collection of lotus objects in a Vector of apropriate wrapper objects
	 * 
	 * @param lotusColl
	 *            the object-collection to wrap
	 * @param schema
	 *            the schema that ensures type safeness
	 * @param parent
	 *            the parent element
	 * @param <T>
	 *            the generic org.openntf.domino type (wrapper)
	 * @param <D>
	 *            the generic lotus.domino type (delegate)
	 * @param <P>
	 *            the generic org.openntf.domino type (parent)
	 * 
	 * @return a vector with wrapper-objects
	 */
	@SuppressWarnings("rawtypes")
	<T extends Base, D extends lotus.domino.Base, P extends Base> Vector<T> fromLotusAsVector(final Collection<?> lotusColl,
			FactorySchema<T, D, P> schema, P parent);

	/**
	 * Wraps a collection of mixed objects in a Vector of apropriate objects. It ensures that Name/DateTime/DateRange objects are
	 * encapsulated correctly.
	 * 
	 * @param values
	 *            the objects to convert
	 * @param schema
	 *            the schema that ensures type safeness
	 * @param parent
	 *            the parent element
	 * @return a vector with wrapper-objects
	 */
	Vector<Object> wrapColumnValues(final Collection<?> values, final org.openntf.domino.Session session);

	/**
	 * Method to unwrap a object
	 * 
	 * @param the
	 *            object to unwrap
	 * @return the unwrapped object
	 */
	<T extends lotus.domino.Base> T toLotus(T base);

	/**
	 * shuts down the factory
	 */
	void terminate();

}