package org.openntf.domino.tests.rpr.formula.eval;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;

import org.openntf.domino.DateTime;

public class ValueHolder extends AbstractList<Object> {
	private Object values[];
	private int size;

	public ValueHolder() {
	}

	/**
	 * Init a ValueHolder based on a single value or a collection
	 * 
	 * @param init
	 */
	public ValueHolder(final Object init) {
		if (init instanceof Collection) {
			addAll((Collection<?>) init);
		} else {
			grow(1);
			add(init);
		}
	}

	/*
	 * for permutative operation
	 */
	public void growP(final int inc1, final int inc2) {
		grow(inc1 * inc2);
	}

	/*
	 * for normal operation
	 */
	public void grow(final int inc1, final int inc2) {
		grow(Math.max(inc1, inc2));
	}

	/**
	 * grows the valueHolder, so that you can efficiently add values with the "add" method
	 * 
	 * @param inc
	 *            the value to increment
	 */
	public void grow(final int inc) {
		Object arr[] = new Object[size + inc];
		if (size > 0) {
			System.arraycopy(values, 0, arr, 0, size);
		}
		values = arr;
	}

	/**
	 * get the nth entry (0=first entry)
	 * 
	 * @param i
	 * @return
	 */
	@Override
	public Object get(final int i) {
		if (size == 0) {
			return null; // TODO: What to do?
		} else if (i < size) {
			return values[i];
		} else {
			return values[size - 1];
		}
	}

	public String getText(final int i) {
		Object o = get(i);
		if (o instanceof String) {
			return (String) o;
		}
		throw new ClassCastException("Text expected. Got '" + o + "'");
	}

	public DateTime getDateTime(final int i) {
		Object o = get(i);
		if (o instanceof DateTime) {
			return (DateTime) o;
		}
		throw new ClassCastException("DateTime expected. Got '" + o + "'");
	}

	public int getInt(final int i) {
		Object o = get(i);
		if (o instanceof Number) {
			return ((Number) o).intValue();
		}
		throw new ClassCastException("Cannot convert " + o + " to number");
	}

	public double getDouble(final int i) {
		Object o = get(i);
		if (o instanceof Double) {
			return ((Double) o);
		}
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		throw new ClassCastException("Cannot convert " + o + " to double");
	}

	public boolean isTrue() {
		for (int i = 0; i < size(); i++) {
			if (getInt(i) != 0) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends Object> other) {
		// gives performance
		grow(other.size());
		return super.addAll(other);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.toString(values);
	}

	@Override
	public int size() {
		return size;
	}

	//	@Override
	//	public Value clone() {
	//		if (multiValue == null) {
	//			return new Value(singleValue);
	//		} else {
	//			return new Value(multiValue);
	//		}
	//	}

	@Override
	public boolean add(final Object other) {
		if (values == null || size >= values.length) {
			grow(4);
		}
		values[size++] = other;
		return true;
	}

	@Override
	public void clear() {
		size = 0;
	}

	//	@Override
	//	public Iterator<T> iterator() {
	//		return new ValueHolderIterator<T>(this);
	//	}

	//	@Override
	//	public boolean remove(final Object arg) {
	//		int found = 0;
	//		for (int i = 0; i < size(); i++) {
	//			T my = get(i);
	//			if (my == null) {
	//				if (arg == null)
	//					found++;
	//
	//			} else {
	//				if (my.equals(arg))
	//					found++;
	//			}
	//			if (found > 0)
	//				values[i] = values[i - found];
	//		}
	//		size -= found;
	//		return found > 0;
	//	}

	//	@Override
	//	public boolean removeAll(final Collection<?> arg0) {
	//		boolean found = false;
	//		for (Object el : arg0) {
	//			if (remove(el))
	//				found = true;
	//
	//		}
	//		return found;
	//	}

	//	@Override
	//	public boolean retainAll(final Collection<?> arg0) {
	//		int found = 0;
	//		for (int i = 0; i < size(); i++) {
	//			T my = get(i);
	//			if (!arg0.contains(my))
	//				found++;
	//
	//			if (found > 0)
	//				values[i] = values[i - found];
	//		}
	//		size -= found;
	//		return found > 0;
	//	}

	@Override
	public Object[] toArray() {
		Object[] ret = new Object[size];
		System.arraycopy(values, 0, ret, 0, size);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] arg0) {
		if (size > arg0.length) {
			Class<?> localClass = arg0.getClass().getComponentType();
			arg0 = (T[]) Array.newInstance(localClass, size);
		}
		System.arraycopy(values, 0, arg0, 0, size);
		if (size < arg0.length) {
			arg0[size] = null;
		}
		return arg0;
	}

}
