package org.kissfarm.shared.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.kissfarm.shared.api.Compressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * This compression impl is here primarily because it supports file permissions
 * 
 * @author sergeyk
 *
 */
public class CompressorTarGzImpl implements Compressor {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public String getExtension() {
		return "tar.gz";
	}

	@Override
	public void compress(Set<File> packageContents, File compressedFile) {
		try {
			Preconditions.checkArgument(packageContents != null && packageContents.size() > 0,
					"Arhcive contents must be provided");
			Preconditions.checkArgument(compressedFile != null, "Target archived file name must be provided");

			try (TarGzFile archive = new TarGzFile(compressedFile.toPath())) {
				packageContents.forEach(x -> {
					try {
						if (x.isDirectory()) {
							archive.bundleDirectory(x.toPath());
						} else {
							archive.bundleFile(x.toPath());
						}
					} catch (Throwable t2) {
						throw new RuntimeException("Failed to add entry to tar.gz file: " + x, t2);
					}
				});
			}

		} catch (Throwable t) {
			throw new RuntimeException(
					"Failed to compress " + packageContents.size() + " item(s) to archive: " + compressedFile, t);
		}
	}

	@Override
	public void decompress(File compressedFile, File targetDirectory) {
		try {
			Preconditions.checkState(targetDirectory.exists() || targetDirectory.mkdirs(),
					"failed to ensure parent folder: %s", targetDirectory);

			try (TarArchiveInputStream fin = new TarArchiveInputStream(
					new GzipCompressorInputStream(new FileInputStream(compressedFile)))) {
				TarArchiveEntry entry;
				while ((entry = fin.getNextTarEntry()) != null) {
					if (entry.isDirectory()) {
						continue;
					}
					File curfile = new File(targetDirectory, entry.getName());
					File parent = curfile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					IOUtils.copy(fin, new FileOutputStream(curfile));
					setFilePermissions(entry, curfile);
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to decompress file " + compressedFile + " to " + targetDirectory, t);
		}
	}

	private void setFilePermissions(TarArchiveEntry entry, File file) {
		try {
			Files.setAttribute(file.toPath(), "unix:mode", entry.getMode());
		} catch (IOException | UnsupportedOperationException e) {
			// we don't really care about Windows in this case
			log.debug("Failed to set file " + file + " permissions " + entry.getMode(), e);
		}
	}
}

/**
 * Archiver impl Copy-pasted from
 * https://gist.github.com/MrSystem/3d7e3c64bb345729065963d2c2bed76f
 * 
 * @author sergeyk
 *
 */
class TarGzFile implements AutoCloseable {
	private static final PathMatcher TAR_GZ_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.tar.gz");

	private final ArchiveOutputStream out;

	public TarGzFile(Path path) throws IOException {
		if (path == null)
			throw new NullPointerException("path must not be null.");
		if (!TAR_GZ_MATCHER.matches(path))
			throw new IllegalArgumentException("path must be a *.tar.gz file.");

		Files.createDirectories(path.getParent());
		out = new TarArchiveOutputStream(
				new GzipCompressorOutputStream(new BufferedOutputStream(Files.newOutputStream(path))));
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	public void bundleFile(Path path) throws IOException {
		if (path == null)
			throw new NullPointerException("path must not be null.");
		if (!Files.isRegularFile(path))
			throw new IllegalArgumentException("path must be an existing file.");

		doBundleFile(path, path.getFileName().toString());
	}

	private void doBundleFile(Path path, String fileName) throws IOException {
		TarArchiveEntry entry = new TarArchiveEntry(path.toFile(), fileName);
		entry.setMode(getUnixFileMode(path));
		out.putArchiveEntry(entry);
		try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
			IOUtils.copy(in, out);
		}
		out.closeArchiveEntry();
	}

	private int getUnixFileMode(Path path) {
		try {
			return (int) Files.getAttribute(path, "unix:mode");
		} catch (IOException | UnsupportedOperationException e) {
			// fallback for non-posix environments
			// NOTE: This impl is not an ideal impl but at least it tracks if file is
			// executable
			int mode = TarArchiveEntry.DEFAULT_FILE_MODE;
			if (Files.isDirectory(path)) {
				mode = TarArchiveEntry.DEFAULT_DIR_MODE;
			} else if (Files.isExecutable(path)) {
				mode |= 0111; // equiv to +x for user/group/others
			}
			return mode;
		}
	}

	public void bundleDirectory(Path path) throws IOException {
		if (path == null)
			throw new NullPointerException("path must not be null.");
		if (!Files.isDirectory(path))
			throw new IllegalArgumentException("path must be an existing directory.");

		FileVisitor<Path> visitor = new DirectoryBundler(path);
		Files.walkFileTree(path, visitor);
	}

	private final class DirectoryBundler extends SimpleFileVisitor<Path> {
		private final Path path, name;

		private DirectoryBundler(Path path) {
			this.path = path;
			name = path.getFileName();
		}

		@Override
		public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) throws IOException {
			String fileName = entryName(directory);

			if (!fileName.equals("")) {
				ArchiveEntry entry = new TarArchiveEntry(directory.toFile(), fileName);
				out.putArchiveEntry(entry);
				out.closeArchiveEntry();
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
			String fileName = entryName(file);

			doBundleFile(file, fileName);

			return FileVisitResult.CONTINUE;
		}

		private String entryName(Path path) {
			return name.resolve(this.path.relativize(path)).toString();
		}
	}
}
