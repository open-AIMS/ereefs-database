/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.helper;

import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.TemporalDomainBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VariableMetadataBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateCanvasBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateDefaultsBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateIdBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateInputBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLayerBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLegendBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateNetCDFTrueColourVariableBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateNetCDFVariableBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePanelBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateRegionBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderMapBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderVideoBean;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.manager.MetadataManager;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigManager;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class used to simplify interaction with the database.
 *
 * <p>This class relates to the {@link NcAnimateConfigBean}
 * and the NcAnimate configuration parts,
 * used with the {@code ereefs-ncanimate2} project.</p>
 */
public class NcAnimateConfigHelper {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateConfigHelper.class);

    private DatabaseClient dbClient;
    private ConfigManager configManager;
    private ConfigPartManager configPartManager;

    // Key: inputDefinitionId (aka downloadDefinitionId)
    // Value: Map
    //     Key: metadata id (usually the NetCDF filename)
    //     Value: NetCDFMetadataBean (extracted NetCDF metadata)
    private static Map<String, Map<String, NetCDFMetadataBean>> netCDFValidMetadataMapCache;
    private static Map<String, Map<String, NetCDFMetadataBean>> netCDFAllMetadataMapCache;

    /**
     * @deprecated Use NcAnimateConfigHelper(DatabaseClient dbClient, CacheStrategy cacheStrategy)
     */
    @Deprecated
    public NcAnimateConfigHelper(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Creates a {@code NcAnimateConfigHelper} using a database client and a cache strategy.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public NcAnimateConfigHelper(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        this.dbClient = dbClient;
        this.configManager = new ConfigManager(this.dbClient, cacheStrategy);
        this.configPartManager = new ConfigPartManager(this.dbClient, cacheStrategy);
    }

    /**
     * Set the {@link MetadataManager} cache strategy.
     * @param cacheStrategy the new cache strategy.
     */
    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.configManager.setCacheStrategy(cacheStrategy);
        this.configPartManager.setCacheStrategy(cacheStrategy);
    }

    /**
     * Clear the {@link MetadataManager} cache strategy.
     * @throws IOException if something goes wrong while clearing the disk cache.
     */
    public void clearCache() throws IOException {
        this.configManager.clearCache();
    }

    /**
     * Returns the {@link DatabaseClient} that was set in the constructor.
     * @return the {@link DatabaseClient}.
     */
    public DatabaseClient getDbClient() {
        return this.dbClient;
    }

    /**
     * Returns an {@code Iterable} list of all the {@link NcAnimateConfigBean}
     * found in the database.
     *
     * @return an {@code Iterable} list of all the {@link NcAnimateConfigBean}.
     * @throws Exception if the database is unreachable.
     */
    public Iterable<NcAnimateConfigBean> getAllNcAnimateConfig() throws Exception {
        JSONObjectIterable jsonConfigs = this.configManager.selectAll();

        Iterator<JSONObject> jsonConfigIterator = jsonConfigs.iterator();
        return new Iterable<NcAnimateConfigBean>() {
            @Override
            public Iterator<NcAnimateConfigBean> iterator() {
                return new Iterator<NcAnimateConfigBean>() {
                    @Override
                    public boolean hasNext() {
                        return jsonConfigIterator.hasNext();
                    }

                    @Override
                    public NcAnimateConfigBean next() {
                        JSONObject jsonConfig = jsonConfigIterator.next();
                        if (jsonConfig == null) {
                            return null;
                        }
                        try {
                            return NcAnimateConfigHelper.this.combineNcAnimateConfig(jsonConfig, null);
                        } catch(OutOfMemoryError ex) {
                            throw ex;
                        } catch(Exception ex) {
                            LOGGER.error("Error occurred while converting a NcAnimate JSON config to a NcAnimateConfigBean", ex);
                        }
                        return null;
                    }
                };
            }
        };
    }

    /**
     * Retrieve a {@link NcAnimateConfigBean} from the database
     * for a given NcAnimate configuration ID.
     *
     * @param configId the NcAnimate configuration ID.
     * @return the {@link NcAnimateConfigBean} or null if not found.
     * @throws Exception if the database is unreachable.
     */
    public NcAnimateConfigBean getNcAnimateConfig(String configId) throws Exception {
        JSONObject jsonConfig = this.configManager.select(configId);
        return this.combineNcAnimateConfig(jsonConfig, configId);
    }

    private NcAnimateConfigBean combineNcAnimateConfig(JSONObject jsonConfig, String configId) throws Exception {
        if (jsonConfig == null) {
            throw new IllegalStateException(
                    String.format("Configuration not found: The configuration ID %s doesn't exists in the database",
                        configId));
        }

        NcAnimateConfigBean ncAnimateConfig = this.combineParts(new NcAnimateConfigBean(new JSONWrapperObject(jsonConfig)));

        // Check if the configuration is valid (if every fields have been parsed)
        // This is used to prevent confusion due to typos in config
        Set<String> neverVisitedConfigFields = ncAnimateConfig.getNeverVisited();
        if (neverVisitedConfigFields != null && !neverVisitedConfigFields.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder(String.format("Invalid NcAnimate configuration %s%nThe following fields are not supported by NcAnimate:%n", configId));
            for (String neverVisitedConfigField : neverVisitedConfigFields) {
                errorMessage.append("- ").append(neverVisitedConfigField).append(String.format("%n"));
            }
            throw new IllegalStateException(errorMessage.toString());
        }

        this.validateNcAnimateConfig(ncAnimateConfig);

        return ncAnimateConfig;
    }

    /**
     * Validate a NcAnimate configuration document.
     *
     * <p>Performs a crude validation check on the configuration
     * document, after all configuration parts are assembled.
     * Throws {@code IllegalStateException} if the
     * configuration is incomplete.</p>
     *
     * @param ncAnimateConfig the {@link NcAnimateConfigBean} configuration document to validate.
     * @return {@true} if the configuration pass the validation test;
     *     throws an {@code IllegalStateException} otherwise.
     */
    public boolean validateNcAnimateConfig(NcAnimateConfigBean ncAnimateConfig) {
        if (ncAnimateConfig == null) {
            throw new IllegalStateException("Invalid configuration: The parsed configuration is null");
        }

        NcAnimateIdBean ncAnimateIdBean = ncAnimateConfig.getId();
        if (ncAnimateIdBean == null) {
            throw new IllegalStateException(
                    String.format("Invalid configuration: Configuration ID is null:%n%s", ncAnimateConfig.toString()));
        }

        String ncAnimateIdStr = ncAnimateIdBean.getValue();

        // Validate render
        NcAnimateRenderBean renderBean = ncAnimateConfig.getRender();
        if (renderBean == null) {
            throw new IllegalStateException(
                    String.format("Invalid configuration: Render is null for ncAnimate config ID: %s", ncAnimateIdStr));
        }

        Map<String, NcAnimateRenderVideoBean> videos = renderBean.getVideos();
        Map<String, NcAnimateRenderMapBean> maps = renderBean.getMaps();

        int videoCount = videos == null ? 0 : videos.size();
        int mapCount = maps == null ? 0 : maps.size();

        if (videoCount <= 0 && mapCount <= 0) {
            throw new IllegalStateException(
                    String.format("Invalid configuration: Contains no product to render. NcAnimate config ID: %s", ncAnimateIdStr));
        }

        if (videoCount > 0) {
            for (Map.Entry<String, NcAnimateRenderVideoBean> videoEntry : videos.entrySet()) {
                String videoConfigIdStr = videoEntry.getKey();
                NcAnimateRenderVideoBean videoBean = videoEntry.getValue();

                if (videoBean == null) {
                    throw new IllegalStateException(
                            String.format("Invalid configuration: Render video ID %s is null. NcAnimate config ID: %s",
                                videoConfigIdStr,
                                ncAnimateIdStr));
                }

                NcAnimateRenderVideoBean.VideoFormat videoFormat = videoBean.getFormat();
                if (videoFormat == null) {
                    throw new IllegalStateException(
                            String.format("Invalid configuration: Render video ID %s do not have a defined video format. NcAnimate config ID: %s",
                                videoConfigIdStr,
                                ncAnimateIdStr));
                }
            }
        }

        if (mapCount > 0) {
            for (Map.Entry<String, NcAnimateRenderMapBean> mapEntry : maps.entrySet()) {
                String mapConfigIdStr = mapEntry.getKey();
                NcAnimateRenderMapBean mapBean = mapEntry.getValue();

                if (mapBean == null) {
                    throw new IllegalStateException(
                            String.format("Invalid configuration: Render map ID %s is null. NcAnimate config ID: %s",
                                mapConfigIdStr,
                                ncAnimateIdStr));
                }

                NcAnimateRenderMapBean.MapFormat mapFormat = mapBean.getFormat();
                if (mapFormat == null) {
                    throw new IllegalStateException(
                            String.format("Invalid configuration: Render map ID %s do not have a defined map format. NcAnimate config ID: %s",
                                mapConfigIdStr,
                                ncAnimateIdStr));
                }
            }
        }

        return true;
    }

    private NcAnimateConfigBean combineParts(NcAnimateConfigBean config) throws Exception {
        // Regions
        Map<String, NcAnimateRegionBean> regionMap = config.getRegions();
        if (regionMap != null) {
            Set<String> regionIds = new HashSet<String>(regionMap.keySet());
            for (String regionId : regionIds) {
                NcAnimateRegionBean regionOverwrites = regionMap.get(regionId);

                JSONObject jsonRegion = this.configPartManager.select(ConfigPartManager.Datatype.REGION, regionId);
                if (jsonRegion != null) {
                    // Region found in DB, apply overwrites found in the JSON config (if any)
                    NcAnimateRegionBean region = new NcAnimateRegionBean(new JSONWrapperObject(jsonRegion));

                    region.overwrite(regionOverwrites);
                    regionMap.put(regionId, region);
                    config.addAllNeverVisited("regions." + regionId, region.getNeverVisited());
                    config.addLastModifiedConfigPart(region);
                } else if (regionOverwrites == null) {
                    // If jsonRegion AND regionOverwrites are null
                    //   Region not found in DB and no overwrites provided (i.e. the region is null)
                    //     { "burdekin": null }
                    //   The config or DB needs to be manually fixed.
                    //   The entry is removed from the config to reduce noise.
                    //   The system will generate other regions so the developer can see that the configuration works,
                    //     but some regions are missing.
                    LOGGER.error(String.format("Region ID \"%s\" could not be found in the database. The region will be ignored.", regionId));
                    regionMap.remove(regionId);
                } else {
                    // Last case (unlikely to happen): Region not found in DB, but some overwrites found in the config.
                    //   The overwrites is used as a "one off" region (region declared in config)
                    //   In that case, the config is expected to be already good, nothing needs to be done.
                    LOGGER.info(String.format("Region ID \"%s\" could not be found in the database. Using region config found in NcAnimate config:%n%s", regionId, regionOverwrites));
                }
            }
        }

        // Canvas
        NcAnimateCanvasBean canvasOverwrites = config.getCanvas();
        if (canvasOverwrites != null) {
            NcAnimateIdBean canvasId = canvasOverwrites.getId();
            if (canvasId != null) {
                String canvasIdStr = canvasId.getValue();
                if (canvasIdStr != null && !canvasIdStr.isEmpty()) {
                    JSONObject jsonCanvas = this.configPartManager.select(ConfigPartManager.Datatype.CANVAS, canvasIdStr);
                    if (jsonCanvas != null) {
                        NcAnimateCanvasBean canvas = new NcAnimateCanvasBean(new JSONWrapperObject(jsonCanvas));
                        canvasOverwrites.overwrite(canvas, canvasOverwrites);
                        config.addAllNeverVisited("canvas", canvas.getNeverVisited());
                        config.addLastModifiedConfigPart(canvas);
                    }
                }
            }
        }

        // Panels
        NcAnimateDefaultsBean defaults = config.removeDefaults();
        NcAnimatePanelBean defaultPanel = null;
        NcAnimateLegendBean defaultLegend = null;
        if (defaults != null) {
            defaultLegend = defaults.getLegend();
            this.combineLegendParts(config, defaultLegend);
            defaultPanel = defaults.getPanel();
            this.combinePanelParts(config, defaultPanel, defaultLegend);
            config.addAllNeverVisited("defaults", defaults.getNeverVisited());
        }

        List<NcAnimatePanelBean> panels = config.getPanels();
        if (panels != null && !panels.isEmpty()) {
            for (int i=0; i<panels.size(); i++) {
                NcAnimatePanelBean panel = panels.get(i);
                NcAnimateIdBean panelId = panel.getId();
                String panelIdStr = panelId == null ? null : panelId.getValue();
                // If the panel ID in the same as the default panel ID (the panel didn't overwrite the ID attribute), use the panel index.
                if (panelId != null && defaultPanel != null && panelId.equals(defaultPanel.getId())) {
                    panelIdStr = null;
                }
                if (panelIdStr == null) {
                    panelIdStr = ""+i;
                }

                panel.overwrite(defaultPanel, panel);
                this.combinePanelParts(config, panel, defaultLegend);
                config.addAllNeverVisited("panels["+panelIdStr+"]", panel.getNeverVisited());
            }
        }

        // Render
        NcAnimateRenderBean renderOverwrites = config.getRender();
        if (renderOverwrites != null) {
            NcAnimateIdBean renderId = renderOverwrites.getId();
            if (renderId != null) {
                String renderIdStr = renderId.getValue();
                if (renderIdStr != null && !renderIdStr.isEmpty()) {
                    JSONObject jsonRender = this.configPartManager.select(ConfigPartManager.Datatype.RENDER, renderIdStr);
                    if (jsonRender != null) {
                        NcAnimateRenderBean render = new NcAnimateRenderBean(new JSONWrapperObject(jsonRender));
                        renderOverwrites.overwrite(render, renderOverwrites);
                        config.addAllNeverVisited("render", render.getNeverVisited());
                        config.addLastModifiedConfigPart(render);
                    }
                }
            }
        }

        return config;
    }

    private void combineLegendParts(NcAnimateConfigBean config, NcAnimateLegendBean legend) throws Exception {
        if (legend != null) {
            // Legend part
            NcAnimateIdBean legendId = legend.getId();
            if (legendId != null) {
                String legendIdStr = legendId.getValue();
                if (legendIdStr != null && !legendIdStr.isEmpty()) {
                    JSONObject jsonDbLegend = this.configPartManager.select(ConfigPartManager.Datatype.LEGEND, legendIdStr);
                    if (jsonDbLegend != null) {
                        NcAnimateLegendBean dbLegend = new NcAnimateLegendBean(new JSONWrapperObject(jsonDbLegend));
                        legend.overwrite(dbLegend, legend);
                        legend.addAllNeverVisited(null, dbLegend.getNeverVisited());
                        config.addLastModifiedConfigPart(dbLegend);
                    }
                }
            }
        }
    }

    private void combinePanelParts(NcAnimateConfigBean config, NcAnimatePanelBean panel, NcAnimateLegendBean defaultLegend) throws Exception {
        if (panel != null) {
            // Panel part
            NcAnimateIdBean panelId = panel.getId();
            if (panelId != null) {
                String panelIdStr = panelId.getValue();
                if (panelIdStr != null && !panelIdStr.isEmpty()) {
                    JSONObject jsonDbPanel = this.configPartManager.select(ConfigPartManager.Datatype.PANEL, panelIdStr);
                    if (jsonDbPanel != null) {
                        NcAnimatePanelBean dbPanel = new NcAnimatePanelBean(new JSONWrapperObject(jsonDbPanel));
                        panel.overwrite(dbPanel, panel);
                        panel.addAllNeverVisited(null, dbPanel.getNeverVisited());
                        config.addLastModifiedConfigPart(dbPanel);
                    }
                }
            }


            // Layers
            List<NcAnimateLayerBean> layers = panel.getLayers();
            Map<String, NcAnimateLayerBean> layerOverwriteMap = panel.getLayerOverwrites();

            // Load layers from DB and index them
            Map<String, NcAnimateLayerBean> layerIndex = new HashMap<String, NcAnimateLayerBean>();
            if (layers != null) {
                for (NcAnimateLayerBean layerOverwrite : layers) {
                    if (layerOverwrite != null) {
                        NcAnimateIdBean layerId = layerOverwrite.getId();
                        if (layerId != null) {
                            String layerIdStr = layerId.getValue();
                            if (layerIdStr != null && !layerIdStr.isEmpty()) {
                                JSONObject jsonLayer = this.configPartManager.select(ConfigPartManager.Datatype.LAYER, layerIdStr);
                                if (jsonLayer != null) {
                                    NcAnimateLayerBean dbLayer = new NcAnimateLayerBean(new JSONWrapperObject(jsonLayer));
                                    layerOverwrite.overwrite(dbLayer, layerOverwrite); // Change layerOverwrite to "dbLayer overwrite with the values found in layerOverwrite (in NcAnimate config)"
                                    panel.addAllNeverVisited("layers["+layerId+"]", dbLayer.getNeverVisited());
                                    config.addLastModifiedConfigPart(dbLayer);
                                } else {
                                    LOGGER.info(String.format("Layer ID \"%s\" could not be found in the database. Using layer config found in NcAnimate config: %s",
                                        layerId, layerOverwrite));
                                }
                                layerIndex.put(layerIdStr, layerOverwrite);
                            }
                        }
                    }
                }
            }

            // Apply (and remove) layer overwrites
            Set<String> layerIds = new HashSet<String>(layerOverwriteMap.keySet());
            for (String layerId : layerIds) {
                NcAnimateLayerBean layerOverwrite = layerOverwriteMap.remove(layerId);
                NcAnimateLayerBean layer = layerIndex.get(layerId);
                if (layer != null) {
                    layer.overwrite(layerOverwrite);
                    panel.addAllNeverVisited("layers["+layerId+"]", layer.getNeverVisited());
                } else {
                    LOGGER.error(String.format("Layer overwrite for layer ID \"%s\" could not be found in list of layer. The layer overwrite will be ignored:%n%s",
                        layerId, layerOverwrite));
                }
            }

            // Combine layer parts (get variable info from DB)
            for (NcAnimateLayerBean layer : layerIndex.values()) {
                this.combineLayerParts(config, layer, defaultLegend);
                panel.addAllNeverVisited("layers["+layer.getId().getValue()+"]", layer.getNeverVisited());
            }
        }
    }

    private void combineLayerParts(NcAnimateConfigBean config, NcAnimateLayerBean layer, NcAnimateLegendBean defaultLegend) throws Exception {
        // Set parts for layer type netcdf
        if (layer != null) {
            if (NcAnimateLayerBean.LayerType.NETCDF.equals(layer.getType()) ||
                    NcAnimateLayerBean.LayerType.GRIB2.equals(layer.getType())) {

                // Input
                NcAnimateInputBean inputOverwrites = layer.getInput();
                if (inputOverwrites != null) {
                    NcAnimateIdBean inputId = inputOverwrites.getId();
                    if (inputId != null) {
                        String inputIdStr = inputId.getValue();
                        if (inputIdStr != null && !inputIdStr.isEmpty()) {
                            JSONObject jsonInput = this.configPartManager.select(ConfigPartManager.Datatype.INPUT, inputIdStr);

                            if (jsonInput != null) {
                                NcAnimateInputBean input = new NcAnimateInputBean(new JSONWrapperObject(jsonInput));
                                inputOverwrites.overwrite(input, inputOverwrites);
                                layer.addAllNeverVisited("input", input.getNeverVisited());
                                config.addLastModifiedConfigPart(input);
                            } else {
                                LOGGER.error(String.format("Layer input ID \"%s\" could not be found in the database. Using input config found in NcAnimate config:%n%s",
                                    inputIdStr, inputOverwrites));
                            }
                        }
                    }
                }

                // Variable
                NcAnimateNetCDFVariableBean variableOverwrites = layer.getVariable();
                if (variableOverwrites != null) {
                    NcAnimateIdBean variableId = variableOverwrites.getId();
                    if (variableId != null) {
                        String variableIdStr = variableId.getValue();
                        if (variableIdStr != null && !variableIdStr.isEmpty()) {
                            JSONObject jsonVariable = this.configPartManager.select(ConfigPartManager.Datatype.VARIABLE, variableIdStr);
                            if (jsonVariable != null) {
                                NcAnimateNetCDFVariableBean variable = new NcAnimateNetCDFVariableBean(new JSONWrapperObject(jsonVariable));
                                variableOverwrites.overwrite(variable, variableOverwrites);

                                layer.addAllNeverVisited("variable["+variableIdStr+"]", variable.getNeverVisited());
                                config.addLastModifiedConfigPart(variable);
                            } else {
                                LOGGER.error(String.format("Layer variable ID \"%s\" could not be found in the database. Using variable config found in NcAnimate config:%n%s",
                                    variableIdStr, variableOverwrites));
                            }
                        }
                    }

                    if (defaultLegend != null) {
                        NcAnimateLegendBean legendConfOverwrite = variableOverwrites.getLegend();
                        if (legendConfOverwrite == null) {
                            legendConfOverwrite = defaultLegend;
                            this.combineLegendParts(config, legendConfOverwrite);
                            variableOverwrites.setLegend(legendConfOverwrite);
                        } else {
                            this.combineLegendParts(config, legendConfOverwrite);
                            legendConfOverwrite.overwrite(defaultLegend, legendConfOverwrite);
                        }

                        variableOverwrites.addAllNeverVisited("legend", legendConfOverwrite.getNeverVisited());
                    }
                }

                // Arrow variable
                NcAnimateNetCDFVariableBean arrowVariableOverwrites = layer.getArrowVariable();
                if (arrowVariableOverwrites != null) {
                    NcAnimateIdBean arrowVariableId = arrowVariableOverwrites.getId();
                    if (arrowVariableId != null) {
                        String arrowVariableIdStr = arrowVariableId.getValue();
                        if (arrowVariableIdStr != null && !arrowVariableIdStr.isEmpty()) {
                            JSONObject jsonArrowVariable = this.configPartManager.select(ConfigPartManager.Datatype.VARIABLE, arrowVariableIdStr);
                            if (jsonArrowVariable != null) {
                                NcAnimateNetCDFVariableBean arrowVariable = new NcAnimateNetCDFVariableBean(new JSONWrapperObject(jsonArrowVariable));
                                arrowVariableOverwrites.overwrite(arrowVariable, arrowVariableOverwrites);
                                layer.addAllNeverVisited("arrowVariable["+arrowVariableIdStr+"]", arrowVariable.getNeverVisited());
                                config.addLastModifiedConfigPart(arrowVariable);
                            } else {
                                LOGGER.error(String.format("Layer arrowVariable ID \"%s\" could not be found in the database. Using variable config found in NcAnimate config:%n%s",
                                    arrowVariableIdStr, arrowVariableOverwrites));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the metadata of all the valid NetCDF files used by a given {@link NcAnimateConfigBean}.
     *
     * <p>The {@code Map} returned is structured as followed:</p>
     * <ul>
     *     <li><em>Key</em>: {@code inputDefinitionId} (aka {@code downloadDefinitionId})</li>
     *     <li><em>Value</em>: {@code Map}
     *         <ul>
     *             <li><em>Key</em>: metadata ID (usually the NetCDF filename)</li>
     *             <li><em>Value</em>: {@link NetCDFMetadataBean} (metadata extracted from the NetCDF file)</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param ncAnimateConfig the {@link NcAnimateConfigBean} configuration to parse.
     * @param metadataHelper the {@link MetadataHelper} to use to retrieve NetCDF metadata from the database.
     * @return a {@code Map} of NetCDF file, as described above.
     * @throws Exception if the database is unreachable.
     */
    public static Map<String, Map<String, NetCDFMetadataBean>> getValidNetCDFMetadataMap(NcAnimateConfigBean ncAnimateConfig, MetadataHelper metadataHelper) throws Exception {
        return NcAnimateConfigHelper.getNetCDFMetadataMap(ncAnimateConfig, metadataHelper, true);
    }

    /**
     * Returns the metadata of all the NetCDF files used by a given {@link NcAnimateConfigBean}.
     *
     * <p>See: {@link #getValidNetCDFMetadataMap(NcAnimateConfigBean, MetadataHelper)}.</p>
     *
     * @param ncAnimateConfig the {@link NcAnimateConfigBean} configuration to parse.
     * @param metadataHelper the {@link MetadataHelper} to use to retrieve NetCDF metadata from the database.
     * @return a {@code Map} of NetCDF file.
     * @throws Exception if the database is unreachable.
     */
    public static Map<String, Map<String, NetCDFMetadataBean>> getAllNetCDFMetadataMap(NcAnimateConfigBean ncAnimateConfig, MetadataHelper metadataHelper) throws Exception {
        return NcAnimateConfigHelper.getNetCDFMetadataMap(ncAnimateConfig, metadataHelper, false);
    }

    private static Map<String, Map<String, NetCDFMetadataBean>> getNetCDFMetadataMap(
            NcAnimateConfigBean ncAnimateConfig,
            MetadataHelper metadataHelper,
            boolean onlyValid) throws Exception {

        if (netCDFValidMetadataMapCache == null) {
            netCDFValidMetadataMapCache = new HashMap<String, Map<String, NetCDFMetadataBean>>();
        }
        if (netCDFAllMetadataMapCache == null) {
            netCDFAllMetadataMapCache = new HashMap<String, Map<String, NetCDFMetadataBean>>();
        }

        Map<String, Map<String, NetCDFMetadataBean>> cache = onlyValid ? netCDFValidMetadataMapCache : netCDFAllMetadataMapCache;

        List<NcAnimatePanelBean> panels = ncAnimateConfig.getPanels();
        if (panels != null) {
            for (NcAnimatePanelBean panel : panels) {
                List<NcAnimateLayerBean> layers = panel.getLayers();
                if (layers != null) {
                    for (NcAnimateLayerBean layer : layers) {
                        NcAnimateInputBean input = layer.getInput();
                        if (input != null) {
                            NcAnimateIdBean inputDefinitionId = input.getId();
                            if (inputDefinitionId != null) {
                                String inputDefinitionIdStr = inputDefinitionId.getValue();
                                if (inputDefinitionIdStr != null && !inputDefinitionIdStr.isEmpty()) {
                                    // Cache the NetCDF file metadata request, to avoid duplicate DB queries.
                                    // NOTE: In many case, all the panels will use the same layers,
                                    //     so caching those request will save significant processing time.
                                    if (!cache.containsKey(inputDefinitionIdStr)) {
                                        Map<String, NetCDFMetadataBean> netCDFMetadataMap =
                                                getMetadataMap(metadataHelper, inputDefinitionIdStr, onlyValid);

                                        if (netCDFMetadataMap != null) {
                                            cache.put(
                                                inputDefinitionIdStr,
                                                netCDFMetadataMap
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return cache;
    }

    private static Map<String, NetCDFMetadataBean> getMetadataMap(MetadataHelper metadataHelper, String inputDefinitionIdStr, boolean onlyValid) throws Exception {
        Iterable<NetCDFMetadataBean> netCDFMetadataIterable = onlyValid ?
                metadataHelper.getValidNetCDFMetadatas(inputDefinitionIdStr) :
                metadataHelper.getAllNetCDFMetadatas(inputDefinitionIdStr);

        if (netCDFMetadataIterable == null) {
            return null;
        }

        Map<String, NetCDFMetadataBean> netCDFMetadataMap = new HashMap<String, NetCDFMetadataBean>();
        for (NetCDFMetadataBean netCDFMetadata : netCDFMetadataIterable) {
            if (netCDFMetadata != null) {
                netCDFMetadataMap.put(netCDFMetadata.getId(), netCDFMetadata);
            }
        }

        return netCDFMetadataMap;
    }

    /**
     * Clear the NetCDF metadata memory cache.
     */
    public static void clearMetadataCache() {
        if (netCDFValidMetadataMapCache != null) {
            netCDFValidMetadataMapCache.clear();
            netCDFValidMetadataMapCache = null;
        }
        if (netCDFAllMetadataMapCache != null) {
            netCDFAllMetadataMapCache.clear();
            netCDFAllMetadataMapCache = null;
        }
    }


    /**
     * Returns the most significant NetCDF variable from a NetCDF layer.
     *
     * <p>Variables are considered in the following order:</p>
     *
     * <ol>
     *     <li><em>variable</em>: Variable rendered as a coloured raster.</li>
     *     <li><em>arrowVariable</em>: Variable used to render arrows.</li>
     *     <li><em>trueColourVariables</em>: Set of variable used to render a
     *         "true colour" representation of what we would see on a satellite image,
     *         rendered using wave length variables. Returns the first non-null
     *         wave length variable define in the layer configuration.</li>
     * </ol>
     *
     * @param layer the {@link NcAnimateLayerBean} to parse.
     * @return the most significant NetCDF variable defined by the layer.
     */
    public static NcAnimateNetCDFVariableBean getMostSignificantVariable(NcAnimateLayerBean layer) {
        if (layer == null) {
            return null;
        }

        NcAnimateNetCDFVariableBean variable = layer.getVariable();
        if (variable != null) {
            return variable;
        }

        NcAnimateNetCDFVariableBean arrowVariable = layer.getArrowVariable();
        if (arrowVariable != null) {
            return arrowVariable;
        }

        Map<String, NcAnimateNetCDFTrueColourVariableBean> trueColourVariables = layer.getTrueColourVariables();
        if (trueColourVariables != null) {
            for (NcAnimateNetCDFTrueColourVariableBean trueColourVariable : trueColourVariables.values()) {
                if (trueColourVariable != null) {
                    return trueColourVariable;
                }
            }
        }

        return null;
    }

    /**
     * Returns a variable metadata found in a {@link NetCDFMetadataBean} metadata document.
     *
     * @param metadata the {@link NetCDFMetadataBean} metadata document to parse.
     * @param variableId the ID of the NetCDF variable to extract from the metadata document.
     * @return the metadata of the variable, or null if the variable is not found in the metadata document.
     */
    public static VariableMetadataBean getVariableMetadata(NetCDFMetadataBean metadata, String variableId) {
        if (metadata == null || variableId == null || variableId.isEmpty()) {
            return null;
        }

        Map<String, VariableMetadataBean> variableMetadataMap = metadata.getVariableMetadataBeanMap();
        if (variableMetadataMap == null || variableMetadataMap.isEmpty()) {
            return null;
        }

        return variableMetadataMap.get(variableId);
    }

    /**
     * Returns a variable metadata found in a {@link NetCDFMetadataBean} metadata document.
     *
     * @param metadata the {@link NetCDFMetadataBean} metadata document to parse.
     * @param variable the {@link NcAnimateNetCDFVariableBean} configuration fragment of the
     *     NetCDF variable to extract from the metadata document.
     * @return the metadata of the variable, or null if the variable is not found in the metadata document.
     */
    public static VariableMetadataBean getVariableMetadata(NetCDFMetadataBean metadata, NcAnimateNetCDFVariableBean variable) {
        if (variable == null) {
            return null;
        }

        return NcAnimateConfigHelper.getVariableMetadata(metadata, variable.getVariableId());
    }

    /**
     * Returns a {@code Set} of all the temporal NetCDF variable
     * found in a given {@link NetCDFMetadataBean} metadata document,
     * used by a given {@link NcAnimateConfigBean} configuration document.
     *
     * <p>A temporal NetCDF variable is a variable which provide data for
     * the temporal dimension. i.e. it's a variable with data for
     * multiple dates.</p>
     *
     * @param ncAnimateConfig the {@link NcAnimateConfigBean} configuration to parse.
     * @param metadata the {@link NetCDFMetadataBean} metadata document.
     * @return a {@code Set} of all the temporal NetCDF variable.
     */
    public static Set<NcAnimateNetCDFVariableBean> getUsedTemporalVariables(NcAnimateConfigBean ncAnimateConfig, NetCDFMetadataBean metadata) {
        if (ncAnimateConfig == null || metadata == null) {
            return null;
        }

        List<NcAnimatePanelBean> panels = ncAnimateConfig.getPanels();
        if (panels == null || panels.isEmpty()) {
            return null;
        }

        Set<NcAnimateNetCDFVariableBean> temporalVariables = new HashSet<NcAnimateNetCDFVariableBean>();

        String metadataDefinitionId = metadata.getDefinitionId();

        if (metadataDefinitionId != null && !metadataDefinitionId.isEmpty()) {
            for (NcAnimatePanelBean panel : panels) {
                List<NcAnimateLayerBean> layers = panel.getLayers();
                if (layers != null && !layers.isEmpty()) {
                    for (NcAnimateLayerBean layer : layers) {
                        // Check if the metadata received in parameter is used by the current layer
                        NcAnimateInputBean layerInput = layer.getInput();
                        if (layerInput != null && metadataDefinitionId.equals(layerInput.getId().getValue())) {
                            NcAnimateNetCDFVariableBean variable = NcAnimateConfigHelper.getMostSignificantVariable(layer);
                            VariableMetadataBean variableMetadataBean = NcAnimateConfigHelper.getVariableMetadata(metadata, variable);
                            if (NcAnimateConfigHelper.isTemporalVariable(variableMetadataBean)) {
                                temporalVariables.add(variable);
                            }
                        }
                    }
                }
            }
        }

        return temporalVariables;
    }

    private static boolean isTemporalVariable(VariableMetadataBean variable) {
        if (variable != null) {
            TemporalDomainBean timeDomain = variable.getTemporalDomainBean();
            if (timeDomain != null) {
                List<DateTime> dateTimes = timeDomain.getTimeValues();
                if (dateTimes != null && !dateTimes.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }
}
