package de.bwl.bwfla.common.services.net;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author adapted from BalusC
 * @link http://balusc.blogspot.com/2009/02/fileservlet-supporting-resume-and.html
 */
public abstract class HttpExport extends HttpServlet
{
	private static final long serialVersionUID = 8627354027512110874L;
	private static final int DEFAULT_BUFFER_SIZE = 10240;
	private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
	
	public abstract File resolveRequest(String reqStr);
	
	@Override
    protected void doHead(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        File downloadFile = resolveRequest(request.getPathInfo());

        if (downloadFile == null || !downloadFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return; // ???
        }

        long length = downloadFile.length();

        Range full = new Range(0, length - 1, length);
        List<Range> ranges = getRanges(response, request, length);

        // gets MIME type of the file
        String mimeType = "application/octet-stream";

        // modifies response
        String disposition = "inline";
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Disposition",
                disposition + ";filename=\"" + downloadFile.getName() + "\"");
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        if (ranges.isEmpty() || ranges.get(0) == full) {

            // Return full file.
            Range r = full;
            response.setContentType(mimeType);
            response.setHeader("Content-Range",
                    "bytes " + r.start + "-" + r.end + "/" + r.total);
            response.setHeader("Content-Length", String.valueOf(r.length));
        } else if (ranges.size() == 1) {
            // Return single part of file.
            Range r = ranges.get(0);
            response.setContentType(mimeType);
            response.setHeader("Content-Range",
                    "bytes " + r.start + "-" + r.end + "/" + r.total);
            response.setHeader("Content-Length", String.valueOf(r.length));
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
        } else {
            // Return multiple parts of file.
            response.setContentType(
                    "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
            int rangelength = 0;
            for (Range r : ranges) {
                rangelength += r.length;
            }
            response.setContentLength(rangelength);
        }
	}
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		File downloadFile = resolveRequest(request.getPathInfo());
		
		if(downloadFile == null || !downloadFile.exists())
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return; // ???
		}
		
		long length = downloadFile.length();
		
		Range full = new Range(0, length - 1, length);
		List<Range> ranges = getRanges(response, request, length);

		// gets MIME type of the file
		String mimeType = "application/octet-stream";

		// modifies response
		String disposition = "inline";
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setHeader("Content-Disposition", disposition + ";filename=\"" + downloadFile.getName() + "\"");
		response.setHeader("Accept-Ranges", "bytes");
		response.setContentType(mimeType);
		response.setContentLength((int) downloadFile.length());

		RandomAccessFile input = null;
		OutputStream output = null;
		try {
			// Open streams.
			input = new RandomAccessFile(downloadFile.getAbsoluteFile(), "r");
			output = response.getOutputStream();

			if (ranges.isEmpty() || ranges.get(0) == full) {

				// Return full file.
				Range r = full;
				response.setContentType(mimeType);
				response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
				response.setHeader("Content-Length", String.valueOf(r.length));
				copy(input, output, r.start, r.length);
			} else if (ranges.size() == 1) {
				// Return single part of file.
				Range r = ranges.get(0);
				response.setContentType(mimeType);
				response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
				response.setHeader("Content-Length", String.valueOf(r.length));
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
				copy(input, output, r.start, r.length);

			} else {

				// Return multiple parts of file.
				response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

				// Cast back to ServletOutputStream to get the easy println methods.
				ServletOutputStream sos = (ServletOutputStream) output;

				// Copy multi part range.
				for (Range r : ranges) {
					// Add multipart boundary and header fields for every range.
					sos.println();
					sos.println("--" + MULTIPART_BOUNDARY);
					sos.println("Content-Type: " + mimeType);
					sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

					// Copy single part range of multi part range.
					copy(input, output, r.start, r.length);
				}

				// End with multipart boundary.
				sos.println();
				sos.println("--" + MULTIPART_BOUNDARY + "--");
			}
		} finally {
			output.close();
			input.close();
		}

	}

	/**
	 * Returns a substring of the given string value from the given begin index to the given end
	 * index as a long. If the substring is empty, then -1 will be returned
	 * @param value The string value to return a substring as long for.
	 * @param beginIndex The begin index of the substring to be returned as long.
	 * @param endIndex The end index of the substring to be returned as long.
	 * @return A substring of the given string value as long or -1 if substring is empty.
	 */
	private static long sublong(String value, int beginIndex, int endIndex) {
		String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Long.parseLong(substring) : -1;
	}

	/**
	 * Returns true if the given match header matches the given value.
	 * @param matchHeader The match header.
	 * @param toMatch The value to be matched.
	 * @return True if the given match header matches the given value.
	 */
	private static boolean matches(String matchHeader, String toMatch) {
		String[] matchValues = matchHeader.split("\\s*,\\s*");
		Arrays.sort(matchValues);
		return Arrays.binarySearch(matchValues, toMatch) > -1
				|| Arrays.binarySearch(matchValues, "*") > -1;
	}

	/**
	 * Copy the given byte range of the given input to the given output.
	 * @param input The input to copy the given range to the given output for.
	 * @param output The output to copy the given range from the given input for.
	 * @param start Start of the byte range.
	 * @param length Length of the byte range.
	 * @throws IOException If something fails at I/O level.
	 */
	private static void copy(RandomAccessFile input, OutputStream output, long start, long length)
			throws IOException
			{
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int read;

		if (input.length() == length) {
			// Write full range.
			while ((read = input.read(buffer)) > 0) {
				output.write(buffer, 0, read);
			}
		} else {
			// Write partial range.
			input.seek(start);
			long toRead = length;

			while ((read = input.read(buffer)) > 0) {
				if ((toRead -= read) > 0) {
					output.write(buffer, 0, read);
				} else {
					output.write(buffer, 0, (int) toRead + read);
					break;
				}
			}
		}
			}

	
	private  List<Range> getRanges(HttpServletResponse response, HttpServletRequest request, long length)
	{
		List<Range> ranges = new ArrayList<Range>();
		String range = request.getHeader("Range");
		if (range != null) {

			// Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
			if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
				response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
				try {
					response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				} catch (IOException e) {
					// nothing to do
				}
				return null;
			}

			// If any valid If-Range header, then process each part of byte range.
			if (ranges.isEmpty()) {
				for (String part : range.substring(6).split(",")) {
					// Assuming a file with length of 100, the following examples returns bytes at:
					// 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
					long start = sublong(part, 0, part.indexOf("-"));
					long end = sublong(part, part.indexOf("-") + 1, part.length());

					if (start == -1) {
						start = length - end;
						end = length - 1;
					} else if (end == -1 || end > length - 1) {
						end = length - 1;
					}

					// Check if Range is syntactically valid. If not, then return 416.
					if (start > end) {
						response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
						try {
							response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}

					// Add range.
					ranges.add(new Range(start, end, length));
				}
			}
		}
		return ranges;
	}
	
	protected class Range {
		long start;
		long end;
		long length;
		long total;

		/**
		 * Construct a byte range.
		 * @param start Start of the byte range.
		 * @param end End of the byte range.
		 * @param total Total length of the byte source.
		 */
		public Range(long start, long end, long total) {
			this.start = start;
			this.end = end;
			this.length = end - start + 1;
			this.total = total;
		}

	}
	
}
