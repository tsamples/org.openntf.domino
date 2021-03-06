/* Generated By:JJTree&JavaCC: Do not edit this line. AtFormulaParser.java */
package de.foconis.test.formula;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jline.Terminal;

import lotus.domino.NotesException;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.ext.Session.Fixes;
import org.openntf.domino.thread.DominoThread;
import org.openntf.domino.utils.DominoUtils;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Strings;
import org.openntf.formula.ASTNode;
import org.openntf.formula.EvaluateException;
import org.openntf.formula.FormulaContext;
import org.openntf.formula.FormulaParseException;
import org.openntf.formula.FormulaParser;
import org.openntf.formula.Formulas;

public class TestRunner extends TestRunnerCommon {
	protected Database db;

	private boolean VIRTUAL_CONSOLE = false;

	public static void main(final String[] args) {
		DominoThread thread = new DominoThread(new TestRunner(), "My thread");
		thread.start();
	}

	public TestRunner() {
		// whatever you might want to do in your constructor, but stay away from Domino objects
		VIRTUAL_CONSOLE = Terminal.getTerminal().getTerminalWidth() < 10;
	}

	@Override
	public void run() {
		try {

			for (Fixes fix : Fixes.values())
				Factory.getSession().setFixEnable(fix, true);
			DominoUtils.setBubbleExceptions(true);

			File file = new File("tests/");
			FilenameFilter filefilter = new FilenameFilter() {
				public boolean accept(final File dir, final String name) {
					return name.endsWith(".txt") || dir.isDirectory();
				}
			};

			// Reading directory contents
			File[] files = file.listFiles(filefilter);

			for (int i = 0; i < files.length; i++) {
				System.out.println(files[i]);
				BufferedReader br = new BufferedReader(new FileReader(files[i]));
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (Strings.isBlankString(line)) {

					} else if (line.startsWith("#")) {
						NTF(line);
					} else {
						execute(line, true, true, true);
					}
				}
				br.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(Factory.dumpCounters(true));
		db = null;
		Factory.terminate();
		System.out.println(Factory.dumpCounters(true));
	}

	private Document createDocument() {
		if (db == null)
			db = Factory.getSession().getDatabase("", "log.nsf");
		try {
			Document doc = db.createDocument();
			return doc;
		} catch (NullPointerException npe) {
			System.err.println("Cannot create demo doc. Is your server running?");
			return null;
		}
	}

	protected void execute(final String line, final boolean testLotus, final boolean testDoc, final boolean testMap) {
		// TODO Auto-generated method stub

		List<Object> ntfDocResult = null;
		List<Object> ntfMapResult = null;
		List<Object> lotusResult = null;
		Throwable ntfError = null;
		boolean lotusFailed = false;
		boolean parserFailed = false;
		// Setup procedure, prepare the demo docs & maps
		StringBuffer errors = new StringBuffer();

		double rnd = Math.random();
		Document ntfDoc = createDocument();
		Document lotusDoc = createDocument();
		Map<String, Object> ntfMap = new HashMap<String, Object>();

		fillDemoDoc(ntfDoc, rnd);
		fillDemoDoc(lotusDoc, rnd);
		fillDemoDoc(ntfMap, rnd);
		lotus.domino.Session rawSession = Factory.toLotus(Factory.getSession());
		lotus.domino.Document rawDocument = Factory.toLotus(lotusDoc);
		if (testLotus) {
			try {

				lotusResult = rawSession.evaluate(line, rawDocument);
			} catch (NotesException e) {
				errors.append(LOTUS("\tLotus failed: ") + ERROR(e) + "\n");
				lotusFailed = true;
			} catch (Throwable t) {
				System.err.println(ERROR("FATAL") + LOTUS("\tLotus failed: ") + ERROR(t));
			}
		}

		// benchmark the AtFormulaParser
		ASTNode ast = null;
		FormulaParser parser = Formulas.getParser();
		try {
			ast = parser.parse(line);
		} catch (FormulaParseException e) {
			errors.append(NTF("\tParser failed: ") + ERROR(e) + "\n");
			e.printStackTrace();
			parserFailed = true;
		} catch (Throwable t) {
			System.err.println(ERROR("FATAL") + NTF("\tParser failed: ") + ERROR(t));
			t.printStackTrace();
		}

		if (!parserFailed) {
			if (testDoc) {
				try {
					FormulaContext ctx1 = Formulas.createContext(ntfDoc, parser);
					ntfDocResult = ast.solve(ctx1);
				} catch (EvaluateException e) {
					errors.append(NTF("\tDoc-Evaluate failed: ") + ERROR(e) + "\n");
					ntfError = e;
					parserFailed = true;
				} catch (Throwable t) {
					System.err.println(ERROR("FATAL") + NTF("\tDoc-Evaluate failed: ") + ERROR(t));
					t.printStackTrace();
				}
			}
			if (testMap) {
				try {
					// benchmark the evaluate with a map as context
					FormulaContext ctx2 = Formulas.createContext(ntfMap, parser);
					ntfMapResult = ast.solve(ctx2);
				} catch (EvaluateException e) {
					errors.append(NTF("\tMap-Evaluate failed: ") + ERROR(e) + "\n");
					ntfError = e;
					parserFailed = true;
				} catch (Throwable t) {
					System.err.println(ERROR("FATAL") + NTF("\tMap-Evaluate failed: ") + ERROR(t));
					t.printStackTrace();
				}
			}
		}

		if (lotusFailed && parserFailed) {
			System.out.println(SUCCESS() + dump(line + " = UNDEFINED"));

			return;
		}

		if (testLotus && testDoc) {
			if (compareList(ntfDocResult, lotusResult)) {
				System.out.println(SUCCESS() + line + " = " + dump(ntfDocResult));
			} else {
				System.err.println(FAIL() + NTF("DOC:") + line);
				System.err.println("\tResult:   " + dump(ntfDocResult) + " Size: " + ((ntfDocResult == null) ? 0 : ntfDocResult.size()));
				System.err.println("\tExpected: " + dump(lotusResult) + " Size: " + ((lotusResult == null) ? 0 : lotusResult.size()));
				if (parserFailed || lotusFailed) {
					System.err.println(errors.toString());
					if (ntfError != null) {
						ntfError.printStackTrace(System.err);
					}
				}
				BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
				try {
					console.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} else {
			if (parserFailed) {
				ntfError.printStackTrace();
			}
			System.err.println("\tDocResult:   " + dump(ntfDocResult) + " Size: " + ((ntfDocResult == null) ? 0 : ntfDocResult.size()));
			System.err.println("\tMapResult:   " + dump(ntfMapResult) + " Size: " + ((ntfMapResult == null) ? 0 : ntfMapResult.size()));
		}
		System.out.println(NTF("Read fields\t") + ast.getReadFields());
		System.out.println(NTF("Modified fields\t") + ast.getModifiedFields());
		System.out.println(NTF("Variables\t") + ast.getVariables());
		System.out.println(NTF("Functions\t") + ast.getFunctions());

	}
}
