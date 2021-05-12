/*
 *  Copyright (C) 2020 Australian Institute of Marine Science
 *
 *  Contact: Gael Lafond <g.lafond@aims.gov.au>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
     * @return {@code true} if the variable pass the validation test; {@code false} if corrupted data if found.
     * @throws IOException exception which may occur while reading an inaccessible or corrupted NetCDF file.
     * @throws InvalidRangeException exception which may occur while reading a corrupted NetCDF file.
     */
    public static boolean scanVariable(RandomAccessFile raf, Variable variable) throws IOException, InvalidRangeException {
        if (!NetCDF4VariableDataScanner.isNetCDF4Variable(variable)) {
            return false;
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
        return true;
    }
}
