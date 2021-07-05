/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs;

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
     * @return {@true} of the file is valid; {@code false} otherwise.
     * @throws IOException exception which may occur while reading an inaccessible or corrupted NetCDF file.
     * @throws InvalidRangeException exception which may occur while reading a corrupted NetCDF file.
     */
    public static boolean scan(File netCDFFile) throws IOException, InvalidRangeException {
        LOGGER.info(String.format("Scanning data file: %s", netCDFFile));

        try (NetcdfFile ncfile = NetcdfFile.open(netCDFFile.getAbsolutePath());
                RandomAccessFile raf = RandomAccessFile.acquire(netCDFFile.getAbsolutePath())) {

            // TODO Add support for GRIB files
            NetCDFFileFormat netcdfFileFormat = DataScanner.getNetCDFFileVersion(ncfile);
            switch(netcdfFileFormat) {

                case NETCDF4:
                    boolean netcdf4magicNumberTest = NetCDF4VariableDataScanner.checkMagicNumber(raf);
                    if (!netcdf4magicNumberTest) {
                        LOGGER.error(String.format("Invalid NetCDF4 magic number found in file: %s", netCDFFile));
                        return false;
                    }

                    for (Variable variable : ncfile.getVariables()) {
                        if (NetCDF4VariableDataScanner.isNetCDF4Variable(variable)) {
                            LOGGER.info(String.format("Scanning NetCDF4 variable: %s", variable.getFullName()));

                            boolean nc4ScanValid = NetCDF4VariableDataScanner.scanVariable(raf, variable);

                            if (!nc4ScanValid) {
                                return false;
                            }

                        } else {
                            LOGGER.warn(String.format("Invalid NetCDF4 variable %s class %s",
                                    variable.getFullName(),
                                    variable.getSPobject().getClass().getCanonicalName()));

                            return false;
                        }
                    }
                    break;


                case NETCDF3:
                    boolean netcdf3magicNumberTest = NetCDF3VariableDataScanner.checkMagicNumber(raf);
                    if (!netcdf3magicNumberTest) {
                        LOGGER.error(String.format("Invalid NetCDF3 magic number found in file: %s", netCDFFile));
                        return false;
                    }


                    for (Variable variable : ncfile.getVariables()) {
                        if (NetCDF3VariableDataScanner.isNetCDF3Variable(variable)) {
                            LOGGER.info(String.format("Scanning NetCDF3 variable: %s", variable.getFullName()));

                            boolean nc3ScanValid = NetCDF3VariableDataScanner.scanVariable(raf, variable);

                            if (!nc3ScanValid) {
                                return false;
                            }

                        } else {
                            LOGGER.warn(String.format("Invalid NetCDF3 variable %s class %s",
                                    variable.getFullName(),
                                    variable.getSPobject().getClass().getCanonicalName()));

                            return false;
                        }
                    }
                    break;


                default:
                    LOGGER.error(String.format("Unsupported NetCDF file format: %s", netCDFFile));
                    return false;
            }
        }

        return true;
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
