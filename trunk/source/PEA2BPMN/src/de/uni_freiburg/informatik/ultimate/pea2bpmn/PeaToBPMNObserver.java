package de.uni_freiburg.informatik.ultimate.pea2bpmn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.core.lib.models.ObjectContainer;
import de.uni_freiburg.informatik.ultimate.core.lib.observers.BaseObserver;
import de.uni_freiburg.informatik.ultimate.core.model.models.IElement;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.pea.PhaseEventAutomata;
import de.uni_freiburg.informatik.ultimate.lib.pea.modelchecking.DotWriterNew;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.pea_impl.PeaImplBuilder;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.preferences.Pea2BPMNPreferences;
import de.uni_freiburg.informatik.ultimate.pea2bpmn.req.PEAFragment;

public class PeaToBPMNObserver extends BaseObserver {

	private final ILogger mLogger;
	private final IUltimateServiceProvider mServices;
	private IElement mBoogieAST;

	private IElement mInput;

	public PeaToBPMNObserver(final ILogger logger, final IUltimateServiceProvider services) {
		mLogger = logger;
		mServices = services;
	}
	private void printReq(List<PatternType<?>> rawPatterns,PrintWriter writer) {
		for (PatternType<?> pattern : rawPatterns) {
			writer.println(pattern.toString());
		}
	}

	@Override
	public boolean process(final IElement root) throws Throwable {
		if (!(root instanceof ObjectContainer)) {
			return false;
		}
		mInput = root;
		@SuppressWarnings("unchecked")
		final List<PatternType<?>> rawPatterns = (List<PatternType<?>>) ((ObjectContainer<?>) root).getValue();
//
//		if (!mServices.getProgressMonitorService().continueProcessing()) {
//			return false;
//		}
//		mBoogieAST = generateBoogie(rawPatterns);

		mLogger.info("req 2 BPMN start");

		String path = mServices.getPreferenceProvider(Activator.PLUGIN_ID)
				.getString(Pea2BPMNPreferences.DUMP_PATH_LABEL);
		String filename = mServices.getPreferenceProvider(Activator.PLUGIN_ID)
				.getString(Pea2BPMNPreferences.FILE_NAME_LABEL);
		final PrintWriter writer = openTempFile(path, filename);
		if (writer == null) {
			mLogger.info("req 2 BPMN end 1");
			return false;
//			final BoogieOutput output = new BoogieOutput(writer);
//			output.printBoogieProgram(unit);
//			printReq(rawPatterns,writer);
//			writer.close();
		}

		ArrayList<PhaseEventAutomata> peas = new ArrayList<>();
		for (PatternType<?> pattern : rawPatterns) {
			mLogger.info("pattern: " + pattern + "\t" + pattern.getClass());
			try {
				PEAFragment pea = new PeaImplBuilder().build(pattern).generate();
				if (pea == null) {
					mLogger.warn("empty pea?");
					continue;
				}
				peas.add(pea);
			} catch (Exception e) {
				mLogger.warn(e.getMessage());
			}
		}

		for (PhaseEventAutomata pea : peas) {
			mLogger.info("write pea: " + pea.getName());
			String dot = DotWriterNew.createDotString(pea);
			writer.println(dot);
		}
		writer.close();

		mLogger.info("req 2 BPMN end 2 " + rawPatterns.size() + "\t" + peas.size());
		return false;
	}

	public IElement getResult() {
		return mInput;
//		return mBoogieAST;
	}

	private PrintWriter openTempFile(final String path, final String filename) {
//		String path;
//		String filename;
		File file = null;
//
//		path = mServices.getPreferenceProvider(Activator.PLUGIN_ID)
//				.getString(Pea2BPMNPreferences.DUMP_PATH_LABEL);

		try {
//			filename = mServices.getPreferenceProvider(Activator.PLUGIN_ID)
//					.getString(Pea2BPMNPreferences.FILE_NAME_LABEL);
			file = new File(path + File.separatorChar + filename);
			if ((!file.isFile() || !file.canWrite()) && file.exists()) {
				mLogger.warn("Cannot write to: " + file.getAbsolutePath());
				return null;
			}

			if (file.exists()) {
				mLogger.info("File already exists and will be overwritten: " + file.getAbsolutePath());
			}
			file.createNewFile();
			mLogger.info("Writing to file " + file.getAbsolutePath());
			return new PrintWriter(new FileWriter(file));

		} catch (final IOException e) {
			if (file != null) {
				mLogger.fatal("Cannot open file: " + file.getAbsolutePath(), e);
			} else {
				mLogger.fatal("Cannot open file", e);
			}
			return null;
		}
	}


}
