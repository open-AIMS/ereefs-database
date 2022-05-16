/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package ucar.nc2.iosp.hdf5;

import org.apache.log4j.Logger;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Variable;
import ucar.nc2.constants.CDM;
import ucar.nc2.iosp.Layout;
import ucar.nc2.iosp.LayoutRegular;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * Scanner used to check if a NetCDF version 4 file contains corrupted data.
 *
 * <p>NOTE: It's present in the {@code ucar.nc2.iosp.hdf5} package
 * to take advantage of some protected methods.</p>
 */
public class NetCDF4VariableDataScanner {
    private static final Logger LOGGER = Logger.getLogger(NetCDF4VariableDataScanner.class);
    private static final byte[] HEAD = new byte[]{-119, 72, 68, 70, 13, 10, 26, 10};

    /**
     * Returns {@code true} if the variable is a NetCDF version 4; {@code false} otherwise.
     *
     * @param variable the variable to check.
     * @return {@code true} if the variable is a NetCDF version 4; {@code false} otherwise.
     */
    public static boolean isNetCDF4Variable(Variable variable) {
        return variable.getSPobject() instanceof H5header.Vinfo;
    }

    /**
     * Returns {@code true} if the magic number found in the NetCDF file
     * matches the expected magic number for a NetCDF version 4; {@code false} otherwise.
     *
     * @param raf the {@link RandomAccessFile} object for the NetCDF file.
     * @return {@code true} if the magic number matches; {@code false} otherwise.
     * @throws IOException if there is a problem reading the underlying data.
     */
    public static boolean checkMagicNumber(RandomAccessFile raf) throws IOException {
        // Inspired from H5header.read
        String hdf5magic = new String(HEAD, CDM.utf8Charset);
        long actualSize = raf.length();

        boolean ok = false;
        for (long filePos = 0L; filePos < actualSize - 8L; filePos = filePos == 0L ? 512L : 2L * filePos) {
            raf.seek(filePos);
            String magic = raf.readString(8);
            if (magic.equals(hdf5magic)) {
                ok = true;
                break;
            }
        }

        return ok;
    }

    /**
     * Read all the data available for a variable, in order to verify if it's corrupted.
     *
     * @param raf the {@link RandomAccessFile} for the NetCDF file containing the data for variable to scan.
     * @param variable the variable to scan.
     * @return Error message if corrupted data is found; null if the variable pass the validation test.
     * @throws IOException exception which may occur while reading an inaccessible or corrupted NetCDF file.
     * @throws InvalidRangeException exception which may occur while reading a corrupted NetCDF file.
     */
    public static String scanVariable(RandomAccessFile raf, Variable variable) throws IOException, InvalidRangeException {
        if (!NetCDF4VariableDataScanner.isNetCDF4Variable(variable)) {
            return String.format("Variable %s is not a NetCDF4 variable.", variable.getShortName());
        }

        Object spObject = variable.getSPobject();

        H5header.Vinfo vinfo = (H5header.Vinfo)spObject;
        Section ranges = variable.getShapeAsSection();

        Layout layout;
        if (vinfo.mfp != null) {
            ByteOrder bo = vinfo.typeInfo.endian == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
            layout = new H5tiledLayoutBB(variable, ranges, raf, vinfo.mfp.getFilters(), bo);
        } else {
            if (vinfo.isChunked) {
                layout = new H5tiledLayout((H5header.Vinfo)variable.getSPobject(), variable.getDataType(), ranges);
            } else {
                layout = new LayoutRegular(vinfo.dataPos, variable.getElementSize(), variable.getShape(), ranges);
            }
        }

        // Loop through the chunks
        // This is what trigger an exception if the file is corrupted.
        while(layout.hasNext()) {
            layout.next();
        }

        // If it gets there without throwing exception, it's a valid NetCDF4 file
        return null;
    }
}
