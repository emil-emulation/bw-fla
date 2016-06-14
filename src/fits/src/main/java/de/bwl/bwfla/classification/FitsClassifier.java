/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.bwl.bwfla.classification.conf.FitsSingleton;
import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.exceptions.FitsException;


/** A wrapper class for the FITS-Tool. */
public class FitsClassifier
{
	/** Logger instance. */
	private static final Logger log = LoggerFactory.getLogger(FitsClassifier.class);

	// Member fields
	private final ThreadLocal<Fits> classifiers;
	private final List<FitsOutput> results;
	private final ExecutorService executor;
	private final FitsClassifier self;
	
	private static final int NUMBER_OF_THREADS = 2 * Runtime.getRuntime().availableProcessors();
	private static final String PROPERTY_FITS_HOME = FitsSingleton.CONF.fitsHome;
	
	/** Constructor */
	public FitsClassifier() throws FitsException
	{
		this.classifiers = new ThreadLocal<Fits>() {
			 @Override protected Fits initialValue() {
				 try {
					return new Fits(PROPERTY_FITS_HOME);
				}
				catch (FitsException exception) {
					exception.printStackTrace();
					return null;
				}
			 }
		};

		this.results = new ArrayList<FitsOutput>(512);
		this.executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		this.self = this;
	}

	/**
	 * Classifies files under the specified path using FITS.
	 * @param input The file or directory to classify.
	 * @param verbose If true, then log-messages will be printed.
	 * @see #FitsClassifier.getResults()
	 */
	public void classify(Path input, final boolean verbose) throws FitsException, IOException, InterruptedException
	{
		if(!FitsSingleton.confValid)
			throw new FitsException("fits module configuration is invalid, will not proceed");
		
		if (verbose)
			log.info("Classifying file(s) in '{}' using {} threads...", input.toString(), NUMBER_OF_THREADS);
		
		if (!Files.isDirectory(input)) {
			this.process(input);
			return;
		}
		
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
			{
				executor.execute(new ClassificationTask(path, verbose));
				return FileVisitResult.CONTINUE;
			}
		};
		
		Files.walkFileTree(input, visitor);
	
		executor.shutdown();
		
		if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
			log.error("Classification of '{}' took too long to finish and was aborted!", input);
			return;
		}
		
		if (verbose)
			log.info("Classification of '{}' finished. {} file(s) processed.", input, results.size());
	}
	
	/** Returns the result of the classification. */
	public List<FitsOutput> getResults()
	{
		return results;
	}
	
	/** Reset this classifier. */
	public void reset()
	{
		results.clear();
	}
	
	
	/* =============== Internal Helpers =============== */
	
	private void process(Path path) throws FitsException
	{
		final File file = path.toFile();
		final Fits fits = classifiers.get();
		FitsOutput output = fits.examine(file);
		
		synchronized (results) {
			results.add(output);
		}
	}
	
	private class ClassificationTask implements Runnable
	{
		private final Path input;
		private final boolean verbose;
		
		public ClassificationTask(Path input, boolean verbose)
		{
			this.input = input;
			this.verbose = verbose;
		}
		
		@Override
		public void run()
		{
			final Path path = input.toAbsolutePath();
			try {
				self.process(path);
				
				if (verbose)
					log.info("Processed '{}'", path);
			}
			catch (FitsException exception) {
				log.warn("Classification of '{}' failed!", path);
				exception.printStackTrace();
			}
		}
	};
}
