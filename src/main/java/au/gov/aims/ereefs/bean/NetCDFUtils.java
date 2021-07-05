/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean;

import au.gov.aims.ereefs.DataScanner;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VariableMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VerticalDomainBean;
import org.apache.log4j.Logger;
import ucar.ma2.InvalidRangeException;
import uk.ac.rdg.resc.edal.dataset.DataReader;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.util.Array4D;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class defining methods which can be used
 * to simplify operations with NetCDF files.
 */
public class NetCDFUtils {
    private static final Logger LOGGER = Logger.getLogger(NetCDFUtils.class);

    private static final float DATA_EPSILON = Float.MIN_NORMAL * 100;

    // Size of a Float mantissa
    private static final float FLOAT_MANTISSA = 1E-6f;
    private static int DATASET_COUNTER = 0;

    /**
     * Returns a {@code uk.ac.rdg.resc.edal.dataset.GriddedDataset} object
     * from a {@code java.io.File} refering to a NetCDF file.
     *
     * @param netCDFFile The NetCDF file.
     * @return {@code uk.ac.rdg.resc.edal.dataset.GriddedDataset} object.
     * @throws IOException if the NetCDF file doesn't exists, can be loaded or is not a gridded dataset.
     */
    public static GriddedDataset getNetCDFDataset(File netCDFFile) throws IOException {
        if (netCDFFile == null || !netCDFFile.exists()) {
            throw new IOException(String.format("The file %s doesn't exists.", netCDFFile));
        }

        String inputFilePath = netCDFFile.getAbsolutePath();

        // Instantiate a dataset factory for reading NetCDF datasets.
        CdmGridDatasetFactory datasetFactory = new CdmGridDatasetFactory();

        // Load the dataset from the input (NetCDF) file.
        Dataset rawDataset = datasetFactory.createDataset("dataset" + (DATASET_COUNTER++), inputFilePath);

        if (!(rawDataset instanceof GriddedDataset)) {
            throw new EdalException("Dataset is not gridded.");
        }
        return (GriddedDataset) rawDataset;
    }

    /**
     * Scan a NetCDF file for corrupted data.
     *
     * @param netCDFFile The file to scan.
     * @return {@code true} if the file is not corrupted; {@code false} otherwise.
     * @throws IOException if the file variable dimensions are inconsistent or if the system runs out of memory.
     */
    public static boolean scan(File netCDFFile) throws IOException {
        try {
            return DataScanner.scan(netCDFFile);
        } catch(InvalidRangeException rangeEx) {
            throw new IOException(String.format("Variable dimensions described in the file header does not match the actual dimensions in the file: %s", netCDFFile), rangeEx);
        } catch(IOException ex) {
            // Look at the cause chain to see if there is a OutOfMemoryError in there
            OutOfMemoryError outOfMemory = getOutOfMemoryErrorCause(ex);
            if (outOfMemory != null) {
                throw outOfMemory;
            }
            throw ex;
        }
    }

    /**
     * Look through an exception stacktrace to find if
     * it was caused by an OutOfMemoryError.
     *
     * @param ex the exception to examine.
     * @return the {@code OutOfMemoryError} that caused the exception, if the exception
     *   was cause by an {@code OutOfMemoryError}; {@code null} otherwise.
     */
    public static OutOfMemoryError getOutOfMemoryErrorCause(Throwable ex) {
        while(ex != null) {
            if (ex instanceof OutOfMemoryError) {
                return (OutOfMemoryError)ex;
            }
            ex = ex.getCause();
        }

        return null;
    }

    /**
     * Calculate the {@code min} and {@code max} value for a
     * NetCDF variable, at 1 and 99 percentiles.
     *
     * <p>Inspired from:
     *   <a href="https://reading-escience-centre.gitbooks.io/edal-user-guide/content/exploring_datasets.html" target="_blank">https://reading-escience-centre.gitbooks.io/edal-user-guide/content/exploring_datasets.html</a></p>
     *
     * <p>NOTE:
     *  NetCDF files often contains <a href="https://en.wikipedia.org/wiki/Outlier" target="_blank">outliers</a>.
     *  The outliers tend to stretch the legend to an unusable extent.
     *  To go around that problem, we store the 1% smaller values in a bin
     *  and the 1% higher values in an other bin, then we get the 1 percentile
     *  and 99 percentile from those bins.</p>
     *
     * <pre class="code">
     *   minBin       ignored data        maxBin
     * [(------)-------------------------(------)]
     *        |                           |
     *  1 percentile =              99 percentile =
     *    minBin.last()               maxBin.first()
     * </pre>
     *
     * @param netCDFFile the NetCDF file containing the dataset.
     * @param variableId the variable ID (aka feature ID) to analise.
     * @param depth the depth or elevation.
     * @return a {@link DataDomain} object, which is a simple container object
     *   for {@code min} and {@code max} values.
     */
    public static DataDomain computeMinMax(File netCDFFile, String variableId, Double depth) throws IOException {
        return NetCDFUtils.computeMinMax(netCDFFile, variableId, depth, null);
    }

    /**
     * Calculate the {@code min} and {@code max} value for a NetCDF variable.
     *
     * <p>See: {@link #computeMinMax(File, String, Double)}</p>
     *
     * @param netCDFFile the NetCDF file containing the dataset.
     * @param variableId the variable ID (aka feature ID) to analise.
     * @param depth the depth or elevation.
     * @param percentile percentile used to ignore outliers.
     *   Set to 0 to get real absolute {@code min} and {@code max} values.
     *   Default: 1 (1 and 99 percentile).
     * @return a {@link DataDomain} object, which is a simple container object
     *   for {@code min} and {@code max} values.
     */
    public static DataDomain computeMinMax(File netCDFFile, String variableId, Double depth, Integer percentile) throws IOException {
        if (percentile == null) {
            percentile = 1;
        }
        // Corrections
        // Percentile bigger than 100 doesn't make sense.
        // Percentile is calculated in a symmetrical manner.
        //   For example, 10 percentile will calculate 10 percentile for min and 90 percentile for max
        if (percentile > 100 || percentile < 0) {
            percentile = 0;
        }
        // Values higher than 50% needs to be adjusted.
        //   For example 75%, is equivalent to [25%, 75%]
        if (percentile > 50) {
            percentile = 100 - percentile;
        }

        LOGGER.info("Computing min / max values for feature '" + variableId + "'");

        long startTime = System.currentTimeMillis(); // For debugging

        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(
                null, null, netCDFFile.toURI(), netCDFFile, netCDFFile.lastModified());

        Map<String, VariableMetadataBean> variables = metadata.getVariableMetadataBeanMap();

        Double closestDepth = null;
        if (variables != null) {
            VariableMetadataBean variableMetadataBean = variables.get(variableId);
            if (variableMetadataBean != null) {
                VerticalDomainBean verticalDomainBean = variableMetadataBean.getVerticalDomainBean();
                if (verticalDomainBean != null) {
                    closestDepth = verticalDomainBean.getClosestHeight(depth == null ? 0.0 : depth);
                }
            }
        }

        Array4D<Number> values = null;
        try {
            values = DataReader.readVariableData(netCDFFile, metadata, variableId, closestDepth);
        } catch(Exception ex) {
            OutOfMemoryError outOfMemory = NetCDFUtils.getOutOfMemoryErrorCause(ex);
            if (outOfMemory != null) {
                throw outOfMemory;
            }
            LOGGER.error("Exception occurred while reading the data for variable ID: " + variableId, ex);
        }

        if (values == null) {
            return null;
        }

        Float minValue = null, maxValue = null;

        long dataReadTime = System.currentTimeMillis(); // For debugging
        LOGGER.debug("Feature " + variableId + " read " + values.size() + " values in " + (dataReadTime - startTime) + "ms");

        // Calculate the population size
        //   The number of none null data points.
        // This is needed to calculate the size of the min / max bins.
        long population = 0;
        for (Number number : values) {
            if (number != null) {
                population++;
            }
        }

        // Divided by 100 to get about 1%
        long binSize = population * percentile / 100;
        // Ensure we get at least 1 value in the bin (when dealing with very small datasets).
        if (binSize <= 0) {
            binSize = 1;
        }
        // NOTE: FullTreeSet is a TreeSet which allow duplicates
        FullTreeSet<Float> minBin = new FullTreeSet<Float>();
        FullTreeSet<Float> maxBin = new FullTreeSet<Float>();

        float val;
        for (Number number : values) {
            if (number != null) {
                val = number.floatValue();
                // Populate the bins
                if (minBin.realSize() < binSize || val < minBin.last()) {
                    minBin.add(val);
                    // Keep the bin within the binSize
                    if (minBin.realSize() > binSize) {
                        minBin.remove(minBin.last());
                    }
                }
                if (maxBin.realSize() < binSize || val > maxBin.first()) {
                    maxBin.add(val);
                    // Keep the bin within the binSize
                    if (maxBin.realSize() > binSize) {
                        maxBin.remove(maxBin.first());
                    }
                }
            }
        }

        if (!minBin.isEmpty()) {
            minValue = minBin.last();
        }
        if (!maxBin.isEmpty()) {
            maxValue = maxBin.first();
        }
        long minMaxTime = System.currentTimeMillis(); // For debugging
        LOGGER.debug("Calculated min / max for feature " + variableId + " in " + (minMaxTime - dataReadTime) + "ms");

        if (minValue == null || maxValue == null) {
            // This should not happen
            LOGGER.warn("Could not calculate the data min / max value for variable ID '" + variableId + "'.");
            return null;
        }

        if (minValue.equals(maxValue)) {
            LOGGER.warn("Variable ID '" + variableId + "' seems to be populated with the constant: " + minValue);
        }

        LOGGER.info("Computed min / max values for feature '" + variableId + "' are [" + minValue + ", " + maxValue + "]");

        return new DataDomain(closestDepth, minValue, maxValue);
    }

    /**
     * Simple container object used to store
     * {@code min} and {@code max} values for
     * a NetCDF variable.
     */
    public static class DataDomain {
        private Double depth;
        private float min, max;

        /**
         * Create a {@code DataDomain}.
         * @param depth the depth or elevation.
         * @param min minimum value.
         * @param max maximum value.
         */
        public DataDomain(Double depth, float min, float max) {
            this.depth = depth;
            this.min = min;
            this.max = max;
        }

        /**
         * Returns the minimum value.
         * @return the minimum value.
         */
        public float getMin() {
            return Math.min(this.min, this.max);
        }

        /**
         * Set the minimum value.
         * @param min the minimum value.
         */
        public void setMin(float min) {
            this.min = min;
        }

        /**
         * Returns the maximum value.
         * @return the maximum value.
         */
        public float getMax() {
            return Math.max(this.min, this.max);
        }

        /**
         * Set the maximum value.
         * @param max the maximum value.
         */
        public void setMax(float max) {
            this.max = max;
        }

        /**
         * Returns the depth or elevation.
         * @return the depth.
         */
        public Double getDepth() {
            return this.depth;
        }

        /**
         * Returns the max value with adjusted
         * decimal to remove noise.
         * This was an attempt at generating clean
         * NetCDF legend, but the approach turned out
         * to be to simple to solve the problem.
         * @deprecated
         */
        @Deprecated
        public float getAdjustedMax() {
            // Get the max values.
            // Assumption: min <= max
            float _min = this.getMin(), _max = this.getMax();

            float range = _max - _min;
            float absoluteMax = Math.max(Math.abs(_max), Math.abs(_min));

            if (range == 0 || range / absoluteMax <= FLOAT_MANTISSA) {
                return _max + NetCDFUtils.FLOAT_MANTISSA * absoluteMax + NetCDFUtils.DATA_EPSILON;
            }
            return _max;
        }

        /**
         * Returns a new {@link DataDomain} containing the same value.
         * @return a copy of the {@link DataDomain}.
         */
        public DataDomain copy() {
            return new DataDomain(this.depth, this.min, this.max);
        }
    }
}
