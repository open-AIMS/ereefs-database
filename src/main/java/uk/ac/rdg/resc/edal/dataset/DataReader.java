/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package uk.ac.rdg.resc.edal.dataset;

import au.gov.aims.ereefs.bean.NetCDFUtils;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.TemporalDomainBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VariableMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VerticalDomainBean;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.metadata.GridVariableMetadata;
import uk.ac.rdg.resc.edal.util.Array4D;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Class used to read data from a NetCDF file.
 *
 * <p>NOTE: It's present in the {@code uk.ac.rdg.resc.edal.dataset} package
 * to take advantage of some protected methods.</p>
 */
public class DataReader {
    private static final Logger LOGGER = Logger.getLogger(DataReader.class);

    /**
     * Reads entire 4D data from a variable, for a given depth.
     *
     * @param netCDFFile the NetCDF file to read.
     * @param metadata the {@link NetCDFMetadataBean} of the NetCDF file.
     * @param variableId the ID of the variable we are aiming to read.
     * @param depth depth value. The actual value on the Z (vertical) dimension, not its index.
     * @return an {@link Array4D} containing the read data.
     * @throws IOException if there is a problem reading the underlying data.
     */
    public static Array4D<Number> readVariableData(File netCDFFile, NetCDFMetadataBean metadata, String variableId, Double depth) throws IOException {
        if (netCDFFile == null || metadata == null) {
            return null;
        }

        Map<String, VariableMetadataBean> variables = metadata.getVariableMetadataBeanMap();
        if (variables == null) {
            return null;
        }

        VariableMetadataBean variableMetadataBean = variables.get(variableId);
        if (variableMetadataBean == null) {
            return null;
        }

        // Find closest Z index
        int zIndex = 0;
        VerticalDomainBean verticalDomainBean = variableMetadataBean.getVerticalDomainBean();
        if (verticalDomainBean != null) {
            Integer heightIndex = verticalDomainBean.getClosestHeightIndex(depth);
            if (heightIndex != null) {
                zIndex = heightIndex;
            }
        }
        LOGGER.debug("readVariableData - variableId: " + variableId + ", zIndex: " + zIndex);

        // Find the index of the last time slice
        int timeIndex = 0;
        TemporalDomainBean temporalDomainBean = variableMetadataBean.getTemporalDomainBean();
        if (temporalDomainBean != null) {
            List<DateTime> timeValues = temporalDomainBean.getTimeValues();
            if (timeValues != null && !timeValues.isEmpty()) {
                timeIndex = timeValues.size() - 1;
            }
        }

        return DataReader.readVariableData(netCDFFile, metadata, variableId, timeIndex, zIndex);
    }

    /**
     * Reads partial 4D data from a variable.
     *
     * <p>Inspired from {@code GriddedDataset.read4dData(String, GridDataSource, GridVariableMetadata)}.</p>
     *
     * @param netCDFFile the NetCDF file to read.
     * @param metadata the {@link NetCDFMetadataBean} of the NetCDF file.
     * @param variableId the ID of the variable we are aiming to read.
     * @param timeIndex the index on the time dimension.
     * @param zIndex the index on the Z (vertical) dimension.
     * @return an {@link Array4D} containing the read data.
     * @throws IOException if there is a problem reading the underlying data.
     */
    public static Array4D<Number> readVariableData(File netCDFFile, NetCDFMetadataBean metadata, String variableId, int timeIndex, int zIndex) throws IOException {
        if (netCDFFile == null || metadata == null) {
            return null;
        }

        Map<String, VariableMetadataBean> variables = metadata.getVariableMetadataBeanMap();
        if (variables == null) {
            return null;
        }

        GriddedDataset dataset = NetCDFUtils.getNetCDFDataset(netCDFFile);
        VariableMetadataBean variableMetadataBean = variables.get(variableId);
        if (variableMetadataBean != null) {
            String variableRole = variableMetadataBean.getRole();
            if (!VectorPlugin.MAG_ROLE.equals(variableRole)) {
                GridVariableMetadata variableMetadata = dataset.getVariableMetadata(variableId);
                GridDataSource gridDataSource = dataset.openDataSource();

                // Find the grid size and read the data
                HorizontalGrid horizontalDomain = variableMetadata.getHorizontalDomain();
                int xSize = horizontalDomain.getXSize();
                int ySize = horizontalDomain.getYSize();

                // NOTE: This doesn't work with vector data (throw NullPointerException when called with MAG_ROLE variables)
                return gridDataSource.read(variableId, timeIndex, timeIndex, zIndex, zIndex,
                        0, ySize - 1,
                        0, xSize - 1);
            }
        }

        return null;
    }
}
