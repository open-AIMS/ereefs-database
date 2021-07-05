/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package ucar.nc2.iosp.netcdf3;

import org.apache.log4j.Logger;
import ucar.ma2.ArrayStructureBB;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.ma2.StructureMembers;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Structure;
import ucar.nc2.Variable;
import ucar.nc2.iosp.IOServiceProvider;
import ucar.nc2.iosp.Layout;
import ucar.nc2.iosp.LayoutRegular;
import ucar.nc2.iosp.LayoutRegularSegmented;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;

/**
 * Scanner used to check if a NetCDF version 3 file contains corrupted data.
 *
 * <p>NOTE: It's present in the {@code ucar.nc2.iosp.netcdf3} package
 * to take advantage of some protected methods.</p>
 */
public class NetCDF3VariableDataScanner {
    private static final Logger LOGGER = Logger.getLogger(NetCDF3VariableDataScanner.class);

    /**
     * Returns {@code true} if the variable is a NetCDF version 3; {@code false} otherwise.
     *
     * @param variable the variable to check.
     * @return {@code true} if the variable is a NetCDF version 3; {@code false} otherwise.
     */
    public static boolean isNetCDF3Variable(Variable variable) {
        return variable.getSPobject() instanceof N3header.Vinfo;
    }

    /**
     * Returns {@code true} if the magic number found in the NetCDF file
     * matches the expected magic number for a NetCDF version 3; {@code false} otherwise.
     *
     * @param raf the {@link RandomAccessFile} object for the NetCDF file.
     * @return {@code true} if the magic number matches; {@code false} otherwise.
     * @throws IOException if there is a problem reading the underlying data.
     */
    public static boolean checkMagicNumber(RandomAccessFile raf) throws IOException {
        // Inspired from N3header.read
        //     https://github.com/mhl/libnetcdf-java/blob/master/cdm/src/main/java/ucar/nc2/iosp/netcdf3/N3header.java
        raf.seek(0);
        byte[] b = new byte[4];
        raf.readFully(b);

        for (int i = 0; i < 3; i++) {
            if (b[i] != N3header.MAGIC[i]) {
                throw new IOException("Not a netCDF file, bad magic number");
            }
        }
        if ((b[3] != 1) && (b[3] != 2)) {
            throw new IOException(String.format("Not a netCDF file, bad magic number at index 3. " +
                    "Expected 1 or 2. Found: %d", b[3]));
        }

        return true;
    }

    /**
     * Read all the data available for a variable, in order to verify if it's corrupted.
     *
     * @param raf the {@link RandomAccessFile} for the NetCDF file containing the data for variable to scan.<br/>
     *     NOTE: This attribute is ignored. It was added to the method definition to be consistent with
     *     {@link ucar.nc2.iosp.hdf5.NetCDF4VariableDataScanner#scanVariable(RandomAccessFile, Variable)}.
     * @param variable the variable to scan.
     * @return {@code true} if the variable pass the validation test; {@code false} if corrupted data if found.
     * @throws IOException exception which may occur while reading an inaccessible or corrupted NetCDF file.
     * @throws InvalidRangeException exception which may occur while reading a corrupted NetCDF file.
     */
    public static boolean scanVariable(RandomAccessFile raf, Variable variable) throws IOException, InvalidRangeException {
        if (!NetCDF3VariableDataScanner.isNetCDF3Variable(variable)) {
            return false;
        }

        // Inspired from
        //     ucar.nc2.iosp.netcdf3.N3iosp.readData(variable, variable.getShapeAsSection());
        NetcdfFile netcdfFile = variable.getNetcdfFile();
        if (netcdfFile == null) {
            throw new IOException(String.format("Variable %s not attached to a NetCDF file", variable.getFullName()));
        }

        IOServiceProvider iosp = netcdfFile.getIosp();
        if (iosp == null) {
            throw new IOException(String.format("NetCDF file has no IOSP: %s", netcdfFile.toString()));
        }

        N3iosp n3iosp = (N3iosp)iosp;
        Section section = variable.getShapeAsSection();
        if (variable instanceof Structure) {
            //return n3iosp.readRecordData((Structure)variable, section);
            Range recordRange = section.getRange(0);
            StructureMembers members = ((Structure)variable).makeStructureMembers();

            ArrayStructureBB structureArray = new ArrayStructureBB(members, new int[]{recordRange.length()});
            structureArray.getByteBuffer();

        } else {
            N3header.Vinfo vinfo = (N3header.Vinfo)variable.getSPobject();
            Layout layout = !variable.isUnlimited() ?
                    new LayoutRegular(vinfo.begin, variable.getElementSize(), variable.getShape(), section) :
                    new LayoutRegularSegmented(vinfo.begin, variable.getElementSize(), n3iosp.header.recsize, variable.getShape(), section);

            // Loop through the chunks
            // This is what trigger an exception if the file is corrupted.
            while(layout.hasNext()) {
                layout.next();
            }
        }


        // If it gets there without throwing exception, it's a valid NetCDF3 file
        return true;
    }
}
