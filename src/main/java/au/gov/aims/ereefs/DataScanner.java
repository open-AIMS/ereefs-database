/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs;

import au.gov.aims.ereefs.bean.NetCDFUtils;
import org.apache.log4j.Logger;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.IOServiceProvider;
import ucar.nc2.iosp.hdf5.H5iosp;
import ucar.nc2.iosp.hdf5.NetCDF4VariableDataScanner;
import ucar.nc2.iosp.netcdf3.N3raf;
import ucar.nc2.iosp.netcdf3.NetCDF3VariableDataScanner;
import ucar.unidata.io.RandomAccessFile;

import java.io.File;
import java.io.IOException;

/**
 * Scanner used to check if a NetCDF file contains corrupted data.
 *
 * <p>It works by reading every byte of data from the NetCDF file.
 * If an exception occurs while reading the data, the file is
 * considered corrupted. It's a slow process, but it's the only
 * reliable solution found so far.</p>
 */
public class DataScanner {
    private static final Logger LOGGER = Logger.getLogger(DataScanner.class);

    /**
     * Loop through all the data for a NetCDF file to verify if it's corrupted.
     *
     * @param netCDFFile the NetCDF file to scan.
     * @return {@code true} of the file is valid; {@code false} otherwise.
     * @throws IOException exception which may occur while reading an inaccessible or corrupted NetCDF file.
     * @throws InvalidRangeException exception which may occur while reading a corrupted NetCDF file.
     */
    public static boolean scan(File netCDFFile) throws IOException {
        return scanWithErrorMessage(netCDFFile) == null;
    }

    public static String scanWithErrorMessage(File netCDFFile) throws IOException {
        LOGGER.info(String.format("Scanning data file: %s", netCDFFile));

        try (NetcdfFile ncfile = NetcdfFile.open(netCDFFile.getAbsolutePath());
                RandomAccessFile raf = RandomAccessFile.acquire(netCDFFile.getAbsolutePath())) {

            // TODO Add support for GRIB files
            NetCDFFileFormat netcdfFileFormat = DataScanner.getNetCDFFileVersion(ncfile);
            switch(netcdfFileFormat) {

                case NETCDF4:
                    boolean netcdf4magicNumberTest = NetCDF4VariableDataScanner.checkMagicNumber(raf);
                    if (!netcdf4magicNumberTest) {
                        String errorMessage = String.format("Invalid NetCDF4 magic number found in file: %s", netCDFFile);
                        LOGGER.error(errorMessage);
                        return errorMessage;
                    }

                    for (Variable variable : ncfile.getVariables()) {
                        if (NetCDF4VariableDataScanner.isNetCDF4Variable(variable)) {
                            LOGGER.info(String.format("Scanning NetCDF4 variable: %s", variable.getFullName()));

                            String nc4ScanErrorMessage;
                            try {
                                nc4ScanErrorMessage = NetCDF4VariableDataScanner.scanVariable(raf, variable);
                            } catch(InvalidRangeException ex) {
                                nc4ScanErrorMessage = String.format("NetCDF4 variable %s dimensions described in the file header does not match the actual dimensions in the file: %s",
                                        variable.getShortName(), netCDFFile);
                            } catch(IOException ex) {
                                // Look at the cause chain to see if there is a OutOfMemoryError in there
                                OutOfMemoryError outOfMemory = NetCDFUtils.getOutOfMemoryErrorCause(ex);
                                if (outOfMemory != null) {
                                    throw outOfMemory;
                                }
                                nc4ScanErrorMessage = String.format("Error occurred while scanning NetCDF4 variable %s: %s",
                                        variable.getShortName(), Utils.getExceptionMessage(ex));
                            }

                            if (nc4ScanErrorMessage != null) {
                                return nc4ScanErrorMessage;
                            }

                        } else {
                            String errorMessage = String.format("Invalid NetCDF4 variable %s class %s",
                                    variable.getFullName(),
                                    variable.getSPobject().getClass().getCanonicalName());

                            LOGGER.warn(errorMessage);

                            return errorMessage;
                        }
                    }
                    break;


                case NETCDF3:
                    boolean netcdf3magicNumberTest = NetCDF3VariableDataScanner.checkMagicNumber(raf);
                    if (!netcdf3magicNumberTest) {
                        String errorMessage = String.format("Invalid NetCDF3 magic number found in file: %s", netCDFFile);
                        LOGGER.error(errorMessage);
                        return errorMessage;
                    }


                    for (Variable variable : ncfile.getVariables()) {
                        if (NetCDF3VariableDataScanner.isNetCDF3Variable(variable)) {
                            LOGGER.info(String.format("Scanning NetCDF3 variable: %s", variable.getFullName()));

                            String nc3ScanErrorMessage;
                            try {
                                nc3ScanErrorMessage = NetCDF3VariableDataScanner.scanVariable(raf, variable);
                            } catch(InvalidRangeException ex) {
                                nc3ScanErrorMessage = String.format("NetCDF3 variable %s dimensions described in the file header does not match the actual dimensions in the file: %s",
                                        variable.getShortName(), netCDFFile);
                            } catch(IOException ex) {
                                // Look at the cause chain to see if there is a OutOfMemoryError in there
                                OutOfMemoryError outOfMemory = NetCDFUtils.getOutOfMemoryErrorCause(ex);
                                if (outOfMemory != null) {
                                    throw outOfMemory;
                                }
                                nc3ScanErrorMessage = String.format("Error occurred while scanning NetCDF3 variable %s: %s",
                                        variable.getShortName(), Utils.getExceptionMessage(ex));
                            }

                            if (nc3ScanErrorMessage != null) {
                                return nc3ScanErrorMessage;
                            }

                        } else {
                            String errorMessage = String.format("Invalid NetCDF3 variable %s class %s",
                                    variable.getFullName(),
                                    variable.getSPobject().getClass().getCanonicalName());

                            LOGGER.warn(errorMessage);

                            return errorMessage;
                        }
                    }
                    break;


                default:
                    String errorMessage = String.format("Unsupported NetCDF file format: %s", netCDFFile);

                    LOGGER.error(errorMessage);
                    return errorMessage;
            }
        }

        return null;
    }

    /**
     * Identifies the NetCDF version of a {@link NetcdfFile}.
     *
     * @param ncfile the {@link NetcdfFile} to check.
     * @return the {@link NetCDFFileFormat}.
     */
    public static NetCDFFileFormat getNetCDFFileVersion(NetcdfFile ncfile) {
        IOServiceProvider iosp = ncfile.getIosp();
        if (iosp != null) {
            if (iosp instanceof H5iosp) {
                return NetCDFFileFormat.NETCDF4;
            }

            if (iosp instanceof N3raf) {
                return NetCDFFileFormat.NETCDF3;
            }
        }

        return NetCDFFileFormat.UNKNOWN;
    }

    /**
     * List of NetCDF file format.
     */
    public enum NetCDFFileFormat {
        NETCDF4, NETCDF3, UNKNOWN
    }
}
