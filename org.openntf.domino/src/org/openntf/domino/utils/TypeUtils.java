/**
 * 
 */
package org.openntf.domino.utils;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import lotus.domino.DateTime;
import lotus.domino.Name;

import org.openntf.domino.Document;
import org.openntf.domino.Item;
import org.openntf.domino.Session;
import org.openntf.domino.exceptions.DataNotCompatibleException;
import org.openntf.domino.exceptions.ItemNotFoundException;
import org.openntf.domino.exceptions.UnimplementedException;
import org.openntf.domino.ext.Formula;
import org.openntf.domino.impl.Base;
import org.openntf.domino.types.BigString;

import com.ibm.icu.math.BigDecimal;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * @author nfreeman
 * 
 */
public enum TypeUtils {
	;

	public static final String[] DEFAULT_STR_ARRAY = { "" };

	@SuppressWarnings("unchecked")
	public static <T> T getDefaultInstance(final Class<?> T) {
		if (T.isArray())
			if (T.getComponentType() == String.class) {
				return (T) DEFAULT_STR_ARRAY.clone();
			} else {
				return (T) Array.newInstance(T.getComponentType(), 0);
			}
		if (Boolean.class.equals(T) || Boolean.TYPE.equals(T))
			return (T) Boolean.FALSE;
		if (Integer.class.equals(T) || Integer.TYPE.equals(T))
			return (T) Integer.valueOf(0);
		if (Long.class.equals(T) || Long.TYPE.equals(T))
			return (T) Long.valueOf(0l);
		if (Short.class.equals(T) || Short.TYPE.equals(T))
			return (T) Short.valueOf("0");
		if (Double.class.equals(T) || Double.TYPE.equals(T))
			return (T) Double.valueOf(0d);
		if (Float.class.equals(T) || Float.TYPE.equals(T))
			return (T) Float.valueOf(0f);
		if (String.class.equals(T))
			return (T) "";
		try {
			return (T) T.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Map<String, Object> toStampableMap(final Map<String, Object> rawMap, final org.openntf.domino.Base<?> context)
			throws IllegalArgumentException {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		synchronized (rawMap) {
			for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
				Object lValue = Base.toItemFriendly(entry.getValue(), context, null);
				result.put(entry.getKey(), lValue);
			}
		}
		return Collections.unmodifiableMap(result);
	}

	@SuppressWarnings("unchecked")
	public static <T> T itemValueToClass(final Document doc, final String itemName, final Class<?> T) {
		String noteid = doc.getNoteID();
		boolean hasItem = doc.hasItem(itemName);
		if (!hasItem) {
			// System.out.println("Item " + itemName + " doesn't exist in document " + doc.getNoteID() + " in "
			// + doc.getAncestorDatabase().getFilePath() + " so we can't return a " + T.getName());
			Class<?> CType = null;
			if (T.isArray()) {
				CType = T.getComponentType();
				if (CType.isPrimitive()) {
					throw new ItemNotFoundException("Item " + itemName + " was not found on document " + noteid
							+ " so we cannot return an array of " + CType.getName());
				} else {
					return null;
				}
			} else if (T.isPrimitive()) {
				throw new ItemNotFoundException("Item " + itemName + " was not found on document " + noteid + " so we cannot return a "
						+ T.getName());
			} else {
				return null;
			}
		}
		Object result = itemValueToClass(doc.getFirstItem(itemName), T);
		if (result != null && !T.isAssignableFrom(result.getClass())) {
			log_.log(Level.WARNING, "Auto-boxing requested a " + T.getName() + " but is returning a " + result.getClass().getName()
					+ " in item " + itemName + " for document id " + noteid);
		}
		return (T) result;
	}

	@SuppressWarnings("rawtypes")
	public static <T> T itemValueToClass(final Item item, final Class<?> T) {
		// Object o = item.getAncestorDocument().getItemValue(item.getName());
		Vector v = item.getValues();
		if (v == null) {
			log_.log(Level.WARNING, "Got a null for the value of item " + item.getName());
		}
		Session session = Factory.getSession(item);
		T result = null;
		try {
			result = vectorToClass(v, T, session);
		} catch (DataNotCompatibleException e) {
			String noteid = item.getAncestorDocument().getNoteID();
			throw new DataNotCompatibleException(e.getMessage() + " for field " + item.getName() + " in document " + noteid);
		} catch (UnimplementedException e) {
			String noteid = item.getAncestorDocument().getNoteID();
			throw new UnimplementedException(e.getMessage() + ", so cannot auto-box for field " + item.getName() + " in document " + noteid);
		}

		return result;
	}

	public static boolean isNumerical(final Object rawObject) {
		boolean result = true;
		if (rawObject == null || rawObject instanceof String)
			return false;	//NTF: we know this is going to be true a LOT, so we'll have a fast out
		if (rawObject instanceof Collection) {
			for (Object obj : (Collection<?>) rawObject) {
				if (!isNumerical(obj)) {
					result = false;
					break;
				}
			}
		} else {
			if (rawObject instanceof Number || Integer.TYPE.isInstance(rawObject) || Double.TYPE.isInstance(rawObject)
					|| Byte.TYPE.isInstance(rawObject) || Short.TYPE.isInstance(rawObject) || Long.TYPE.isInstance(rawObject)
					|| Float.TYPE.isInstance(rawObject)) {
			} else {
				result = false;
			}
		}
		return result;
	}

	public static boolean isCalendrical(final Object rawObject) {
		boolean result = true;
		if (rawObject == null || rawObject instanceof String)
			return false;	//NTF: we know this is going to be true a LOT, so we'll have a fast out
		if (rawObject instanceof Collection) {
			for (Object obj : (Collection<?>) rawObject) {
				if (!isCalendrical(obj)) {
					result = false;
					break;
				}
			}
		} else {
			if (rawObject instanceof DateTime || rawObject instanceof Date) {
			} else {
				result = false;
			}
		}
		return result;
	}

	public static boolean isNameish(final Object rawObject) {
		boolean result = true;
		if (rawObject == null)
			return false;	//NTF: we know this is going to be true a LOT, so we'll have a fast out
		if (rawObject instanceof Collection) {
			for (Object obj : (Collection<?>) rawObject) {
				if (!isNameish(obj)) {
					result = false;
					break;
				}
			}
		} else {
			if (rawObject instanceof String) {
				result = DominoUtils.isHierarchicalName((String) rawObject);
			} else {
				result = false;
			}
		}
		return result;
	}

	public static <T> T objectToClass(final Object o, final Class<?> T, final Session session) {
		if (o == null) {
			return null;
		}
		if (o instanceof Vector) {
			return vectorToClass((Vector<?>) o, T, session);
		}
		Vector<Object> v = new Vector<Object>();
		v.add(o);
		return vectorToClass(v, T, session);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> T vectorToClass(final Vector v, final Class<?> T, final Session session) {
		//		if (T == java.lang.Class.class) {
		//			log_.log(Level.WARNING, "Class type requested from type coersion!");
		//		} else if (T == java.util.Collection.class) {
		//			log_.log(Level.WARNING, "Collection type requested from type coersion!");
		//		}
		if (v == null) {
			return null;
		}
		Object result = null;
		Class<?> CType = null;
		if (T.equals(String[].class)) {
			result = toStrings(v);
			return (T) result;
		}
		if (T.isArray()) {
			if (T == String[].class) {
				// System.out.println("Shallow route to string array");
				result = toStrings(v);
			} else {
				CType = T.getComponentType();
				if (CType.isPrimitive()) {
					try {
						result = toPrimitiveArray(v, CType);
					} catch (DataNotCompatibleException e) {
						throw e;
					}
				} else if (Number.class.isAssignableFrom(CType)) {
					result = toNumberArray(v, CType);
				} else {
					if (CType == String.class) {
						// System.out.println("Deep route to string array");
						result = toStrings(v);
					} else if (CType == BigString.class) {
						result = toBigStrings(v);
					} else if (CType == Pattern.class) {
						result = toPatterns(v);
					} else if (CType == Enum.class) {
						result = toEnums(v);
					} else if (Class.class.isAssignableFrom(CType)) {
						result = toClasses(v);
					} else if (Formula.class.isAssignableFrom(CType)) {
						result = toFormulas(v);
					} else if (CType == Date.class) {
						result = toDates(v);
					} else if (DateTime.class.isAssignableFrom(CType)) {
						result = toDateTimes(v, session);
					} else if (Name.class.isAssignableFrom(CType)) {
						result = toNames(v, session);
					} else if (CType == Boolean.class) {
						result = toBooleans(v);
					} else if (CType == java.lang.Object.class) {
						result = toObjects(v);
					} else {
						throw new UnimplementedException("Arrays for " + CType.getName() + " not yet implemented");
					}
				}
			}
		} else if (T.isPrimitive()) {
			try {
				result = toPrimitive(v, T);
			} catch (DataNotCompatibleException e) {
				throw e;
			}
		} else {
			if (T == String.class) {
				result = join(v);
			} else if (T == Enum.class) {
				String str = join(v);
				//				System.out.println("Attempting to convert string " + str + " to Enum");
				result = toEnum(str);
				//				System.out.println("result was " + (result == null ? "null" : result.getClass().getName()));
			} else if (T == BigString.class) {
				result = new BigString(join(v));
			} else if (T == Pattern.class) {
				result = Pattern.compile(join(v));
			} else if (Class.class.isAssignableFrom(T)) {
				//				try {
				String cn = join(v);
				Class<?> cls = DominoUtils.getClass(cn);
				result = cls;
				//				} catch (ClassNotFoundException e) {
				//					DominoUtils.handleException(e);
				//					result = null;
				//				}
			} else if (Formula.class.isAssignableFrom(T)) {
				Formula formula = new org.openntf.domino.helpers.Formula(join(v));
				result = formula;
			} else if (T == java.util.Collection.class) {
				result = new ArrayList();
				if (v != null) {
					((ArrayList) result).addAll(v);
				}
			} else if (java.util.Collection.class.isAssignableFrom(T)) {
				try {
					result = T.newInstance();
					Collection coll = (Collection) result;
					coll.addAll(DominoUtils.toSerializable(v));
				} catch (IllegalAccessException e) {
					DominoUtils.handleException(e);
				} catch (InstantiationException e) {
					DominoUtils.handleException(e);
				}
			} else if (T == Date.class) {
				result = toDate(v);
			} else if (T == org.openntf.domino.DateTime.class) {
				if (session != null) {
					result = session.createDateTime(toDate(v));
				} else {
					throw new IllegalArgumentException("Cannont convert a Vector to DateTime without a valid Session object");
				}
			} else if (T == org.openntf.domino.Name.class) {
				if (session != null) {
					if (v.isEmpty()) {
						result = session.createName("");
					} else {
						result = session.createName(String.valueOf(v.get(0)));
					}
				} else {
					throw new IllegalArgumentException("Cannont convert a Vector to Name without a valid Session object");

				}
			} else if (T == Boolean.class) {
				if (v.isEmpty()) {
					result = Boolean.FALSE;
				} else {
					result = toBoolean(v.get(0));
				}
			} else {
				if (!v.isEmpty()) {
					if (Number.class.isAssignableFrom(T)) {
						result = toNumber(v, T);
					} else {
						result = v.get(0);
					}
				}
			}
		}

		if (result != null && !T.isAssignableFrom(result.getClass())) {
			log_.log(Level.WARNING, "Auto-boxing requested a " + T.getName() + " but is returning a " + result.getClass().getName());
		}
		return (T) result;
	}

	private static final Logger log_ = Logger.getLogger(TypeUtils.class.getName());

	@SuppressWarnings("unchecked")
	public static <T> T toNumberArray(final Vector<Object> value, final Class<?> T) {
		int size = value.size();
		Object[] result = (Object[]) Array.newInstance(T, size);
		for (int i = 0; i < size; i++) {
			result[i] = toNumber(value.get(i), T);
		}
		return (T) result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T toNumber(final Object value, final Class<?> T) throws DataNotCompatibleException {
		// System.out.println("Starting toNumber to get type " + T.getName() + " from a value of type " + value.getClass().getName());
		if (value == null)
			return null;
		if (value instanceof Vector && (((Vector<?>) value).isEmpty()))
			return null;
		T result = null;
		Object localValue = value;
		if (value instanceof Collection) {
			localValue = ((Collection<?>) value).iterator().next();
		}
		// System.out.println("LocalValue is type " + localValue.getClass().getName() + ": " + String.valueOf(localValue));

		if (T == Integer.class) {
			if (localValue instanceof String) {
				result = (T) Integer.valueOf((String) localValue);
			} else if (localValue instanceof Double) {
				result = (T) Integer.valueOf(((Double) localValue).intValue());
			} else if (localValue instanceof Integer) {
				result = (T) localValue;
			} else if (localValue instanceof Long) {
				result = (T) Integer.valueOf(((Long) localValue).intValue());
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == Long.class) {
			if (localValue instanceof String) {
				result = (T) Long.valueOf((String) localValue);
			} else if (localValue instanceof Double) {
				result = (T) Long.valueOf(((Double) localValue).longValue());
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == Double.class) {
			if (localValue instanceof String) {
				result = (T) Double.valueOf((String) localValue);
			} else if (localValue instanceof Double) {
				result = (T) localValue;
			} else if (localValue instanceof Integer) {
				result = (T) Double.valueOf(((Integer) localValue).doubleValue());
			} else if (localValue instanceof Short) {
				result = (T) Double.valueOf(((Short) localValue).doubleValue());
			} else if (localValue instanceof Float) {
				result = (T) Double.valueOf(((Float) localValue).doubleValue());
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == Short.class) {
			if (localValue instanceof String) {
				result = (T) Short.valueOf((String) localValue);
			} else if (localValue instanceof Double) {
				result = (T) Short.valueOf(((Double) localValue).shortValue());
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == Byte.class) {
			if (localValue instanceof String) {
				result = (T) Byte.valueOf((String) localValue);
			} else if (localValue instanceof Double) {
				result = (T) Byte.valueOf(((Double) localValue).byteValue());
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == Float.class) {
			if (localValue instanceof String) {
				result = (T) Float.valueOf((String) localValue);
			} else if (localValue instanceof Double) {
				result = (T) Float.valueOf(((Double) localValue).floatValue());
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == BigDecimal.class) {
			if (localValue instanceof String) {
				result = (T) new BigDecimal((String) localValue);
			} else if (localValue instanceof Double) {
				result = (T) new BigDecimal((Double) localValue);
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == BigInteger.class) {
			if (localValue instanceof String) {
				result = (T) new BigInteger((String) localValue);
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == AtomicInteger.class) {
			if (localValue instanceof String) {
				result = (T) new AtomicInteger(Integer.valueOf((String) localValue));
			} else if (localValue instanceof Double) {
				result = (T) new AtomicInteger(Integer.valueOf(((Double) localValue).intValue()));
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		} else if (T == AtomicLong.class) {
			if (localValue instanceof String) {
				result = (T) new AtomicLong(Long.valueOf((String) localValue));
			} else if (localValue instanceof Double) {
				result = (T) new AtomicLong(Long.valueOf(((Double) localValue).longValue()));
			} else {
				throw new DataNotCompatibleException("Cannot create a " + T.getName() + " from a " + localValue.getClass().getName());
			}
		}
		return result;
	}

	public static Boolean[] toBooleans(final Collection<Object> vector) {
		if (vector == null || vector.isEmpty())
			return new Boolean[0];
		Boolean[] bools = new Boolean[vector.size()];
		int i = 0;
		for (Object o : vector) {
			bools[i++] = toBoolean(o);
		}
		return bools;
	}

	public static boolean toBoolean(final Object value) {
		if (value instanceof String) {
			char[] c = ((String) value).toCharArray();
			if (c.length > 1 || c.length == 0) {
				return false;
			} else {
				return c[0] == '1';
			}
		} else if (value instanceof Double) {
			if (((Double) value).intValue() == 0) {
				return false;
			} else {
				return true;
			}
		} else {
			throw new DataNotCompatibleException("Cannot convert a " + value.getClass().getName() + " to boolean primitive.");
		}
	}

	public static int toInt(final Object value) {
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		} else if (value instanceof Double) {
			return ((Double) value).intValue();
		} else {
			throw new DataNotCompatibleException("Cannot convert a " + value.getClass().getName() + " to int primitive.");
		}
	}

	public static double toDouble(final Object value) {
		if (value instanceof Integer) {
			return ((Integer) value).doubleValue();
		} else if (value instanceof Double) {
			return ((Double) value).doubleValue();
		} else {
			throw new DataNotCompatibleException("Cannot convert a " + value.getClass().getName() + " to double primitive.");
		}
	}

	public static long toLong(final Object value) {
		if (value instanceof Integer) {
			return ((Integer) value).longValue();
		} else if (value instanceof Double) {
			return ((Double) value).longValue();
		} else {
			throw new DataNotCompatibleException("Cannot convert a " + value.getClass().getName() + " to long primitive.");
		}
	}

	public static short toShort(final Object value) {
		if (value instanceof Integer) {
			return ((Integer) value).shortValue();
		} else if (value instanceof Double) {
			return ((Double) value).shortValue();
		} else {
			throw new DataNotCompatibleException("Cannot convert a " + value.getClass().getName() + " to short primitive.");
		}

	}

	public static float toFloat(final Object value) {
		if (value instanceof Integer) {
			return ((Integer) value).floatValue();
		} else if (value instanceof Double) {
			return ((Double) value).floatValue();
		} else {
			throw new DataNotCompatibleException("Cannot convert a " + value.getClass().getName() + " to float primitive.");
		}

	}

	public static Object toPrimitive(final Vector<Object> values, final Class<?> ctype) {
		if (ctype.isPrimitive()) {
			throw new DataNotCompatibleException(ctype.getName() + " is not a primitive type.");
		}
		if (values.size() > 1) {
			throw new DataNotCompatibleException("Cannot create a primitive " + ctype + " from data because we have a multiple values.");
		}
		if (values.isEmpty()) {
			throw new DataNotCompatibleException("Cannot create a primitive " + ctype + " from data because we don't have any values.");
		}
		if (ctype == Boolean.TYPE)
			return toBoolean(values.get(0));
		if (ctype == Integer.TYPE)
			return toInt(values.get(0));
		if (ctype == Short.TYPE)
			return toShort(values.get(0));
		if (ctype == Long.TYPE)
			return toLong(values.get(0));
		if (ctype == Float.TYPE)
			return toFloat(values.get(0));
		if (ctype == Double.TYPE)
			return toDouble(values.get(0));
		if (ctype == Byte.TYPE)
			throw new UnimplementedException("Primitive conversion for byte not yet defined");
		if (ctype == Character.TYPE)
			throw new UnimplementedException("Primitive conversion for char not yet defined");
		if (ctype == com.ibm.icu.lang.UCharacter.class)
			throw new UnimplementedException("Primitive conversion for char not yet defined");
		throw new DataNotCompatibleException("");
	}

	public static String join(final Object[] values, final String separator) {
		if (values == null || values.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (Object val : values) {
			if (!isFirst) {
				sb.append(separator);
			}
			sb.append(String.valueOf(val));
			isFirst = false;
		}
		return sb.toString();
	}

	public static String join(final Collection<?> values, final String separator) {
		if (values == null || values.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = values.iterator();
		while (it.hasNext()) {
			sb.append(String.valueOf(it.next()));
			if (it.hasNext())
				sb.append(separator);
		}
		return sb.toString();
	}

	public static String join(final Collection<?> values) {
		return join(values, ", ");
	}

	public static String join(final Object[] values) {
		return join(values, ", ");
	}

	public static Object toPrimitiveArray(final Vector<Object> values, final Class<?> ctype) throws DataNotCompatibleException {
		Object result = null;
		int size = values.size();
		if (ctype == Boolean.TYPE) {
			boolean[] outcome = new boolean[size];
			// TODO NTF - should allow for String fields that are binary sequences: "1001001" (SOS)
			for (int i = 0; i < size; i++) {
				Object o = values.get(i);
				outcome[i] = toBoolean(o);
			}
			result = outcome;
		} else if (ctype == Byte.TYPE) {
			byte[] outcome = new byte[size];
			// TODO
			result = outcome;
		} else if (ctype == Character.TYPE) {
			char[] outcome = new char[size];
			// TODO How should this work? Just concatenate the char arrays for each String?
			result = outcome;
		} else if (ctype == Short.TYPE) {
			short[] outcome = new short[size];
			for (int i = 0; i < size; i++) {
				Object o = values.get(i);
				outcome[i] = toShort(o);
			}
			result = outcome;
		} else if (ctype == Integer.TYPE) {
			int[] outcome = new int[size];
			for (int i = 0; i < size; i++) {
				Object o = values.get(i);
				outcome[i] = toInt(o);
			}
			result = outcome;
		} else if (ctype == Long.TYPE) {
			long[] outcome = new long[size];
			for (int i = 0; i < size; i++) {
				Object o = values.get(i);
				outcome[i] = toLong(o);
			}
			result = outcome;
		} else if (ctype == Float.TYPE) {
			float[] outcome = new float[size];
			for (int i = 0; i < size; i++) {
				Object o = values.get(i);
				outcome[i] = toFloat(o);
			}
			result = outcome;
		} else if (ctype == Double.TYPE) {
			double[] outcome = new double[size];
			for (int i = 0; i < size; i++) {
				Object o = values.get(i);
				outcome[i] = toDouble(o);
			}
			result = outcome;
		}
		return result;
	}

	public static Date toDate(Object value) throws DataNotCompatibleException {
		if (value == null)
			return null;
		if (value instanceof Vector && (((Vector<?>) value).isEmpty()))
			return null;
		if (value instanceof Vector) {
			value = ((Vector<?>) value).get(0);
		}
		if (value instanceof Long) {
			return new Date(((Long) value).longValue());
		} else if (value instanceof String) {
			// TODO finish
			DateFormat df = new SimpleDateFormat();
			String str = (String) value;
			if (str.length() < 1)
				return null;
			try {
				return df.parse(str);
			} catch (ParseException e) {
				throw new DataNotCompatibleException("Cannot create a Date from String value " + (String) value);
			}
		} else if (value instanceof lotus.domino.DateTime) {
			return DominoUtils.toJavaDateSafe((lotus.domino.DateTime) value);
		} else {
			throw new DataNotCompatibleException("Cannot create a Date from a " + value.getClass().getName());
		}
	}

	public static Date[] toDates(final Collection<Object> vector) throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new Date[0];

		Date[] result = new Date[vector.size()];
		int i = 0;
		for (Object o : vector) {
			result[i++] = toDate(o);
		}
		return result;
	}

	public static org.openntf.domino.DateTime[] toDateTimes(final Collection<Object> vector, final org.openntf.domino.Session session)
			throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new org.openntf.domino.DateTime[0];

		org.openntf.domino.DateTime[] result = new org.openntf.domino.DateTime[vector.size()];
		if (session != null) {
			int i = 0;
			for (Object o : vector) {
				result[i++] = session.createDateTime(toDate(o));
			}
			return result;
		} else {
			throw new IllegalArgumentException("Cannont convert to DateTime without a valid Session object");
		}
	}

	public static org.openntf.domino.Name[] toNames(final Collection<Object> vector, final org.openntf.domino.Session session)
			throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new org.openntf.domino.Name[0];

		org.openntf.domino.Name[] result = new org.openntf.domino.Name[vector.size()];
		if (session != null) {
			int i = 0;
			for (Object o : vector) {
				result[i++] = session.createName(String.valueOf(o));
			}
			return result;
		} else {
			throw new IllegalArgumentException("Cannont convert to Name without a valid Session object");
		}
	}

	public static String[] toStrings(final Collection<Object> vector) throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new String[0];

		String[] strings = new String[vector.size()];
		int i = 0;
		// strings = vector.toArray(new String[0]);
		for (Object o : vector) {
			if (o instanceof org.openntf.domino.DateTime) {
				strings[i++] = ((org.openntf.domino.DateTime) o).getGMTTime();
			} else {
				strings[i++] = String.valueOf(o);
			}
		}
		return strings;
	}

	public static String toString(final java.lang.Object object) throws DataNotCompatibleException {
		if (object == null)
			return null;
		if (object instanceof String) {
			return (String) object;
		} else if (object instanceof Collection) {
			return join((Collection<?>) object);
		} else if (object.getClass().isArray()) {
			return join((Object[]) object);
		} else {
			return String.valueOf(object);
		}
	}

	public static Pattern[] toPatterns(final Collection<Object> vector) throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new Pattern[0];

		Pattern[] patterns = new Pattern[vector.size()];
		int i = 0;
		for (Object o : vector) {
			patterns[i++] = Pattern.compile(String.valueOf(o));
		}
		return patterns;
	}

	public static java.lang.Object[] toObjects(final Collection<Object> vector) throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new Object[0];

		Object[] patterns = new Object[vector.size()];
		int i = 0;
		for (Object o : vector) {
			patterns[i++] = o;
		}
		return patterns;
	}

	public static Class<?>[] toClasses(final Collection<Object> vector) throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new Class[0];

		@SuppressWarnings("unused")
		ClassLoader cl = Factory.getClassLoader();
		Class<?>[] classes = new Class[vector.size()];
		int i = 0;
		for (Object o : vector) {
			int pos = i++;
			String cn = String.valueOf(o);
			//			try {
			Class<?> cls = DominoUtils.getClass(cn);
			//				Class<?> cls = Class.forName(cn, false, cl);
			classes[pos] = cls;
			//			} catch (ClassNotFoundException e) {
			//				System.out.println("Failed to find class " + cn + " using a classloader of type " + cl.getClass().getName());
			//				DominoUtils.handleException(e);
			//				classes[pos] = null;
			//			}
		}
		return classes;
	}

	public static Enum<?> toEnum(final Object value) throws DataNotCompatibleException {
		//		ClassLoader cl = Factory.getClassLoader();
		//		System.out.println("Enum coercion requested from value " + String.valueOf(value));
		if (value == null)
			return null;
		if (value instanceof Vector && (((Vector<?>) value).isEmpty()))
			return null;
		Enum<?> result = null;
		String en = String.valueOf(value);
		String ename = null;
		String cn = null;
		if (en.indexOf(' ') > 0) {
			cn = String.valueOf(value).substring(0, en.indexOf(' ')).trim();
			ename = String.valueOf(value).substring(en.indexOf(' ') + 1).trim();
		}
		if (cn == null || ename == null) {
			//			System.out.println("ALERT! This isn't going to work. cn is " + String.valueOf(cn) + " and ename is " + String.valueOf(ename));
		} else {
			try {
				Class<?> cls = DominoUtils.getClass(cn);
				//				System.out.println("Enum coercion with class " + cls.getName());
				if (cls != null) {
					Object[] objs = cls.getEnumConstants();
					if (objs.length > 0) {
						//					System.out.println("Enum coercion into " + cn + " with value " + ename + " started...");
						//					StringBuilder typenames = new StringBuilder();
						for (Object obj : objs) {
							if (obj instanceof Enum) {
								if (((Enum<?>) obj).name().equals(ename)) {
									result = (Enum<?>) obj;
									//								System.out.println("Found a match between " + result.name() + " and " + ename + "!");
									return result;
								} else {
									//								typenames.append(", " + ((Enum) obj).name());
								}
							} else {
								//							System.out.println("Expected encounter an Enum constant, but didn't. Instead found a "
								//									+ obj.getClass().getName());
							}
						}
						//					System.out.println("Unable to match " + ename + " with any of: " + typenames.toString());
					} else {
						//					System.out.println("No enum constants found for class " + cls.getName());

					}
				}
			} catch (Exception e) {
				//				System.out.println("Failed to find class " + cn + " using a thread's current classloader");
				DominoUtils.handleException(e);
			}
		}
		return result;
	}

	public static Enum<?>[] toEnums(final Collection<Object> vector) throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new Enum[0];
		ClassLoader cl = Factory.getClassLoader();
		Enum<?>[] classes = new Enum[vector.size()];
		int i = 0;
		for (Object o : vector) {
			int pos = i++;
			String en = String.valueOf(o);
			String ename = null;
			String cn = null;
			if (en.indexOf(' ') > 0) {
				cn = String.valueOf(o).substring(0, en.indexOf(' ')).trim();
				ename = String.valueOf(o).substring(en.indexOf(' ') + 1).trim();
			}
			try {
				Class<?> cls = Class.forName(cn, false, cl);
				for (Object obj : cls.getEnumConstants()) {
					if (obj instanceof Enum) {
						if (((Enum<?>) obj).name().equals(ename)) {
							classes[pos] = (Enum<?>) obj;
						}
					}
				}
			} catch (ClassNotFoundException e) {
				System.out.println("Failed to find class " + cn + " using a classloader of type " + cl.getClass().getName());
				DominoUtils.handleException(e);
				classes[pos] = null;
			}
		}
		return classes;
	}

	public static Formula[] toFormulas(final Collection<Object> vector) throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new Formula[0];
		Formula[] formulas = new Formula[vector.size()];
		int i = 0;
		for (Object o : vector) {
			Formula formula = new org.openntf.domino.helpers.Formula(String.valueOf(o));
			formulas[i++] = formula;
		}
		return formulas;
	}

	public static BigString[] toBigStrings(final Collection<Object> vector) throws DataNotCompatibleException {
		if (vector == null || vector.isEmpty())
			return new BigString[0];
		BigString[] strings = new BigString[vector.size()];
		int i = 0;
		for (Object o : vector) {
			if (o instanceof org.openntf.domino.DateTime) {
				strings[i++] = new BigString(((org.openntf.domino.DateTime) o).getGMTTime());
			} else {
				strings[i++] = new BigString(String.valueOf(o));
			}
		}
		return strings;
	}

	public static int[] toIntArray(final Collection<Integer> coll) {
		int[] ret = new int[coll.size()];
		Iterator<Integer> iterator = coll.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}

	public static short[] toShortArray(final Collection<Short> coll) {
		short[] ret = new short[coll.size()];
		Iterator<Short> iterator = coll.iterator();
		for (int i = 0; i < ret.length; i++) {
			Short s = iterator.next();
			ret[i] = s.shortValue();
		}
		return ret;
	}

	public static long[] toLongArray(final Collection<Long> coll) {
		long[] ret = new long[coll.size()];
		Iterator<Long> iterator = coll.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().longValue();
		}
		return ret;
	}

	public static byte[] toByteArray(final Collection<Byte> coll) {
		byte[] ret = new byte[coll.size()];
		Iterator<Byte> iterator = coll.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().byteValue();
		}
		return ret;
	}

}
