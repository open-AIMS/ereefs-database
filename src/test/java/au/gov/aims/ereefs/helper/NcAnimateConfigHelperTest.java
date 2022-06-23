/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.helper;

import au.gov.aims.ereefs.bean.metadata.TimeIncrementUnit;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLegendBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateNetCDFTrueColourVariableBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderMapBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderMetadataBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderVideoBean;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigManagerTestBase;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateBboxBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateCanvasBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateInputBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLayerBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateNetCDFVariableBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePaddingBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePanelBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateRegionBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateTextBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePositionBean;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NcAnimateConfigHelperTest extends ConfigManagerTestBase {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateConfigHelperTest.class);
    private static final Double EPSILON = 0.0000001;

    @Test
    public void testSelectMapConfig() throws Exception {
        super.insertDummyData();

        NcAnimateConfigHelper configHelper = new NcAnimateConfigHelper(this.getDatabaseClient(), CACHE_STRATEGY);
        NcAnimateConfigBean config = configHelper.getNcAnimateConfig("gbr4_v2_temp-wind-salt-current_maps");

        // Check if all element of the config is used
        Set<String> neverVisited = config.getNeverVisited();
        Assert.assertTrue(String.format("NcAnimate config was not parse properly. The following config were not parsed:%n%s", neverVisited),
                neverVisited == null || neverVisited.isEmpty());
    }

    @Test
    public void testSelectMonthlyConfig() throws Exception {
        super.insertDummyData();

        NcAnimateConfigHelper configHelper = new NcAnimateConfigHelper(this.getDatabaseClient(), CACHE_STRATEGY);
        NcAnimateConfigBean config = configHelper.getNcAnimateConfig("gbr4_v2_temp-wind-salt-current_monthly");

        // Check if all element of the config is used
        Set<String> neverVisited = config.getNeverVisited();
        Assert.assertTrue(String.format("NcAnimate config was not parse properly. The following config were not parsed:%n%s", neverVisited),
                neverVisited == null || neverVisited.isEmpty());
    }

    @Test
    public void testSelectConfig() throws Exception {
        super.insertDummyData();

        NcAnimateConfigHelper configHelper = new NcAnimateConfigHelper(this.getDatabaseClient(), CACHE_STRATEGY);
        NcAnimateConfigBean config = configHelper.getNcAnimateConfig("gbr4_v2_temp-wind-salt-current");

        // Check if all element of the config is used
        Set<String> neverVisited = config.getNeverVisited();
        Assert.assertTrue(String.format("NcAnimate config was not parse properly. The following config were not parsed:%n%s", neverVisited),
                neverVisited == null || neverVisited.isEmpty());

        LOGGER.debug(config.toString());


        Assert.assertEquals("Wrong version number", "2.0", config.getVersion());


        // *****************************************
        // ** TargetHeights [-12.75, -5.55, -1.5] **
        // *****************************************

        List<Double> targetHeights = config.getTargetHeights();
        Assert.assertNotNull("TargetHeights is null", targetHeights);
        Assert.assertEquals("Wrong number of targetHeights", 3, targetHeights.size());
        Assert.assertEquals("Wrong targetHeight (index 0)", -12.75, targetHeights.get(0), EPSILON);
        Assert.assertEquals("Wrong targetHeight (index 1)", -5.55, targetHeights.get(1), EPSILON);
        Assert.assertEquals("Wrong targetHeight (index 2)", -1.5, targetHeights.get(2), EPSILON);


        // *************
        // ** Regions **
        // *************

        Map<String, NcAnimateRegionBean> regions = config.getRegions();
        Assert.assertNotNull("Regions is null", regions);
        Assert.assertEquals("Wrong number of regions", 3, regions.size());

        NcAnimateRegionBean bneRegion = regions.get("brisbane");
        Assert.assertNotNull("Brisbane region is missing", bneRegion);
        Assert.assertEquals("brisbane", bneRegion.getId().getValue());
        Assert.assertEquals("Brisbane", bneRegion.getLabel());
        NcAnimateBboxBean bneBbox = bneRegion.getBbox();
        Assert.assertNotNull("Brisbane bounding box is missing", bneBbox);
        Assert.assertEquals(-22.5, bneBbox.getNorth(), EPSILON);
        Assert.assertEquals(156, bneBbox.getEast(), EPSILON);
        Assert.assertEquals(-28.7, bneBbox.getSouth(), EPSILON);
        Assert.assertEquals(152.13, bneBbox.getWest(), EPSILON);

        NcAnimateRegionBean qldRegion = regions.get("qld");
        Assert.assertNotNull("Queensland region is missing", qldRegion);
        Assert.assertEquals("qld", qldRegion.getId().getValue());
        Assert.assertEquals("Queensland", qldRegion.getLabel());
        NcAnimateBboxBean qldBbox = qldRegion.getBbox();
        Assert.assertNotNull("Queensland bounding box is missing", qldBbox);
        Assert.assertEquals(-7.6, qldBbox.getNorth(), EPSILON);
        Assert.assertEquals(156, qldBbox.getEast(), EPSILON);
        Assert.assertEquals(-29.4, qldBbox.getSouth(), EPSILON);
        Assert.assertEquals(142.4, qldBbox.getWest(), EPSILON);

        NcAnimateRegionBean tsRegion = regions.get("torres-strait");
        Assert.assertNotNull("Torres-Strait region is missing", tsRegion);
        Assert.assertEquals("torres-strait", tsRegion.getId().getValue());
        Assert.assertEquals("Torres Strait", tsRegion.getLabel());
        NcAnimateBboxBean tsBbox = tsRegion.getBbox();
        Assert.assertNotNull("Torres Strait bounding box is missing", tsBbox);
        Assert.assertEquals(-7.45, tsBbox.getNorth(), EPSILON);
        Assert.assertEquals(144.7, tsBbox.getEast(), EPSILON);
        Assert.assertEquals(-12.2, tsBbox.getSouth(), EPSILON);
        Assert.assertEquals(141.8, tsBbox.getWest(), EPSILON);


        // ************
        // ** Canvas **
        // ************

        NcAnimateCanvasBean canvas = config.getCanvas();
        Assert.assertNotNull("Canvas is null", canvas);
        Assert.assertEquals("default-canvas", canvas.getId().getValue());
        Assert.assertEquals("#FFFFFF", canvas.getBackgroundColour());
        Assert.assertEquals("wrong canvas padding between panels", new Integer(16), canvas.getPaddingBetweenPanels());

        NcAnimatePaddingBean canvasPadding = canvas.getPadding();
        Assert.assertNotNull("Canvas padding is null", canvasPadding);
        Assert.assertEquals("Wrong canvas padding top", new Integer(48), canvasPadding.getTop());
        Assert.assertEquals("Wrong canvas padding right", new Integer(16), canvasPadding.getRight());
        Assert.assertEquals("Wrong canvas padding bottom", new Integer(49), canvasPadding.getBottom());
        Assert.assertEquals("Wrong canvas padding left", new Integer(16), canvasPadding.getLeft());

        Map<String, NcAnimateTextBean> canvasTexts = canvas.getTexts();
        Assert.assertNotNull("Canvas texts is null", canvasTexts);
        Assert.assertEquals("Wrong number of canvas texts", 6, canvasTexts.size());

        // Canvas - text - licence
        NcAnimateTextBean licenceCanvasText = canvasTexts.get("licence");
        Assert.assertNotNull("Canvas text licence is missing", licenceCanvasText);
        Assert.assertFalse("Canvas text licence is hidden", licenceCanvasText.isHidden());
        Assert.assertEquals(new Integer(12), licenceCanvasText.getFontSize());
        Assert.assertEquals(false, licenceCanvasText.getBold());
        Assert.assertEquals(false, licenceCanvasText.getItalic());

        NcAnimatePositionBean licenceCanvasTextPosition = licenceCanvasText.getPosition();
        Assert.assertNotNull("Canvas text licence position is missing", licenceCanvasTextPosition);
        Assert.assertNull(licenceCanvasTextPosition.getTop());
        Assert.assertEquals(new Integer(16), licenceCanvasTextPosition.getRight());
        Assert.assertEquals(new Integer(10), licenceCanvasTextPosition.getBottom());
        Assert.assertNull(licenceCanvasTextPosition.getLeft());

        List<String> licenceCanvasTextTexts = licenceCanvasText.getText();
        Assert.assertNotNull("Canvas licence text is missing", licenceCanvasTextTexts);
        Assert.assertEquals("Wrong canvas licence text", "Licensing: ${layers.licences}", licenceCanvasTextTexts.get(0));

        // Canvas - text - region
        NcAnimateTextBean regionCanvasText = canvasTexts.get("region");
        Assert.assertNotNull("Canvas text region is missing", regionCanvasText);
        Assert.assertFalse("Canvas text region is hidden", regionCanvasText.isHidden());
        Assert.assertEquals(new Integer(25), regionCanvasText.getFontSize());
        Assert.assertEquals(false, regionCanvasText.getBold());
        Assert.assertEquals(false, regionCanvasText.getItalic());

        NcAnimatePositionBean regionCanvasTextPosition = regionCanvasText.getPosition();
        Assert.assertNotNull("Canvas text region position is missing", regionCanvasTextPosition);
        Assert.assertEquals(new Integer(28), regionCanvasTextPosition.getTop());
        Assert.assertNull(regionCanvasTextPosition.getRight());
        Assert.assertNull(regionCanvasTextPosition.getBottom());
        Assert.assertEquals(new Integer(16), regionCanvasTextPosition.getLeft());

        List<String> regionCanvasTextTexts = regionCanvasText.getText();
        Assert.assertNotNull("Canvas region text is missing", regionCanvasTextTexts);
        Assert.assertEquals("Wrong canvas region text", "${ctx.region.label}", regionCanvasTextTexts.get(0));

        // Canvas - text - frameDate
        NcAnimateTextBean frameDateCanvasText = canvasTexts.get("frameDate");
        Assert.assertNotNull("Canvas text frameDate is missing", frameDateCanvasText);
        Assert.assertTrue("Canvas text frameDate is visible", frameDateCanvasText.isHidden());
        Assert.assertEquals(new Integer(25), frameDateCanvasText.getFontSize());
        Assert.assertEquals(true, frameDateCanvasText.getBold());
        Assert.assertEquals(false, frameDateCanvasText.getItalic());

        NcAnimatePositionBean frameDateCanvasTextPosition = frameDateCanvasText.getPosition();
        Assert.assertNotNull("Canvas text frameDate position is missing", frameDateCanvasTextPosition);
        Assert.assertEquals(new Integer(30), frameDateCanvasTextPosition.getTop());
        Assert.assertNull(frameDateCanvasTextPosition.getRight());
        Assert.assertNull(frameDateCanvasTextPosition.getBottom());
        Assert.assertNull(frameDateCanvasTextPosition.getLeft());

        List<String> frameDateCanvasTextTexts = frameDateCanvasText.getText();
        Assert.assertNotNull("Canvas frameDate text is missing", frameDateCanvasTextTexts);
        Assert.assertEquals("Wrong canvas frameDate text", "${ctx.frameDate dd-MMM-yyyy}", frameDateCanvasTextTexts.get(0));

        // Canvas - text - frequency
        NcAnimateTextBean frequencyCanvasText = canvasTexts.get("frequency");
        Assert.assertNotNull("Canvas text frequency is missing", frequencyCanvasText);
        Assert.assertFalse("Canvas text frequency is hidden", frequencyCanvasText.isHidden());
        Assert.assertEquals(new Integer(25), frequencyCanvasText.getFontSize());
        Assert.assertEquals(false, frequencyCanvasText.getBold());
        Assert.assertEquals(false, frequencyCanvasText.getItalic());

        NcAnimatePositionBean frequencyCanvasTextPosition = frequencyCanvasText.getPosition();
        Assert.assertNotNull("Canvas text frequency position is missing", frequencyCanvasTextPosition);
        Assert.assertEquals(new Integer(28), frequencyCanvasTextPosition.getTop());
        Assert.assertEquals(new Integer(16), frequencyCanvasTextPosition.getRight());
        Assert.assertNull(frequencyCanvasTextPosition.getBottom());
        Assert.assertNull(frequencyCanvasTextPosition.getLeft());

        List<String> frequencyCanvasTextTexts = frequencyCanvasText.getText();
        Assert.assertNotNull("Canvas frequency text is missing", frequencyCanvasTextTexts);
        Assert.assertEquals("Wrong canvas frequency text", "${ctx.framePeriod}", frequencyCanvasTextTexts.get(0));

        // Canvas - text - authors
        NcAnimateTextBean authorsCanvasText = canvasTexts.get("authors");
        Assert.assertNotNull("Canvas text authors is missing", authorsCanvasText);
        Assert.assertFalse("Canvas text authors is hidden", authorsCanvasText.isHidden());
        Assert.assertEquals(new Integer(12), authorsCanvasText.getFontSize());
        Assert.assertEquals("Wrong canvas text authors bold", false, authorsCanvasText.getBold());
        Assert.assertEquals("Wrong canvas text authors italic", true, authorsCanvasText.getItalic());

        NcAnimatePositionBean authorsCanvasTextPosition = authorsCanvasText.getPosition();
        Assert.assertNotNull("Canvas text authors position is missing", authorsCanvasTextPosition);
        Assert.assertNull(authorsCanvasTextPosition.getTop());
        Assert.assertEquals(new Integer(16), authorsCanvasTextPosition.getRight());
        Assert.assertEquals(new Integer(28), authorsCanvasTextPosition.getBottom());
        Assert.assertNull(authorsCanvasTextPosition.getLeft());

        List<String> authorsCanvasTextTexts = authorsCanvasText.getText();
        Assert.assertNotNull("Canvas authors text is missing", authorsCanvasTextTexts);
        Assert.assertEquals("Wrong canvas authors text", "Data: ${layers.authors}. Map generation: AIMS", authorsCanvasTextTexts.get(0));

        // Canvas - text - fromToDate
        NcAnimateTextBean fromToDateCanvasText = canvasTexts.get("fromToDate");
        Assert.assertNotNull("Canvas text fromToDate is missing", fromToDateCanvasText);
        Assert.assertFalse("Canvas text fromToDate is hidden", fromToDateCanvasText.isHidden());
        Assert.assertEquals(new Integer(25), fromToDateCanvasText.getFontSize());
        Assert.assertEquals("Wrong canvas text fromToDate bold", true, fromToDateCanvasText.getBold());
        Assert.assertNull("Wrong canvas text fromToDate italic", fromToDateCanvasText.getItalic());

        NcAnimatePositionBean fromToDateCanvasTextPosition = fromToDateCanvasText.getPosition();
        Assert.assertNotNull("Canvas text fromToDate position is missing", fromToDateCanvasTextPosition);
        Assert.assertEquals(new Integer(28), fromToDateCanvasTextPosition.getTop());
        Assert.assertNull(fromToDateCanvasTextPosition.getRight());
        Assert.assertNull(fromToDateCanvasTextPosition.getBottom());
        Assert.assertNull(fromToDateCanvasTextPosition.getLeft());

        List<String> fromToDateCanvasTextTexts = fromToDateCanvasText.getText();
        Assert.assertNotNull("Canvas fromToDate text is missing", fromToDateCanvasTextTexts);
        Assert.assertEquals("Wrong canvas fromToDate text", "${ctx.dateFrom yyyy-MM-dd} - ${ctx.dateTo yyyy-MM-dd}", fromToDateCanvasTextTexts.get(0));


        // ************
        // ** Panels **
        // ************

        // Verify that the default panel has been removed.
        // That's not very important, but it should not belong there after it has been merged into the layers.
        Assert.assertNull("NcAnimate defaults has not been removed", config.getDefaults());

        List<NcAnimatePanelBean> panels = config.getPanels();
        Assert.assertNotNull("Panels is null", panels);
        Assert.assertEquals("Wrong number of panels", 5, panels.size());

        // Temperature panel
        NcAnimatePanelBean tempPanel = panels.get(0);
        Assert.assertNotNull("Panel temp is null", tempPanel);
        Assert.assertEquals("temp", tempPanel.getId().getValue());

        NcAnimateTextBean tempPanelTitle = tempPanel.getTitle();
        Assert.assertNotNull("Panel temp title is null", tempPanelTitle);
        Assert.assertEquals("Wrong panel temp title fontSize", new Integer(30), tempPanelTitle.getFontSize());

        NcAnimatePositionBean tempPanelTitlePosition = tempPanelTitle.getPosition();
        Assert.assertNotNull("Panel temp title position is null", tempPanelTitlePosition);
        Assert.assertEquals("Wrong panel temp title position top", new Integer(35), tempPanelTitlePosition.getTop());
        Assert.assertNull("Wrong panel temp title position right", tempPanelTitlePosition.getRight());
        Assert.assertNull("Wrong panel temp title position bottom", tempPanelTitlePosition.getBottom());
        Assert.assertEquals("Wrong panel temp title position left", new Integer(25), tempPanelTitlePosition.getLeft());

        Assert.assertNull("Panel temp title bold is not null", tempPanelTitle.getBold());
        Assert.assertNull("Panel temp title italic is not null", tempPanelTitle.getItalic());

        List<String> tempPanelTitleTexts = tempPanelTitle.getText();
        Assert.assertNotNull("Panel temp title text is null", tempPanelTitleTexts);
        Assert.assertEquals("Wrong panel temp title text", "Temperature ${ctx.targetHeight %.1f}m", tempPanelTitleTexts.get(0));

        List<NcAnimateLayerBean> tempPanelLayers = tempPanel.getLayers();
        Assert.assertNotNull("Panel temp layers is null", tempPanelLayers);
        Assert.assertEquals("Wrong number of panel temp layers", 10, tempPanelLayers.size());

        NcAnimateLayerBean tempPanelLayer0 = tempPanelLayers.get(0);
        Assert.assertNotNull("Panel temp layer 0 is null", tempPanelLayer0);
        Assert.assertEquals("Wrong temp layer 0 id", "ereefs-model_gbr4-v2", tempPanelLayer0.getId().getValue());
        Assert.assertEquals("Wrong temp layer 0 type", NcAnimateLayerBean.LayerType.NETCDF, tempPanelLayer0.getType());
        Assert.assertEquals("Wrong temp layer 0 targetHeight", "${ctx.targetHeight}", tempPanelLayer0.getTargetHeight());
        Assert.assertNull("Temp layer 0 arrowSize is not null", tempPanelLayer0.getArrowSize());
        NcAnimateInputBean tempPanelLayer0Input = tempPanelLayer0.getInput();
        Assert.assertNotNull("Panel temp layer 0 input is null", tempPanelLayer0Input);
        Assert.assertEquals("Wrong temp layer 0 input id", "downloads/gbr4_v2", tempPanelLayer0Input.getId().getValue());
        List<String> tempPanelLayer0InputAuthors = tempPanelLayer0Input.getAuthors();
        Assert.assertNotNull("Panel temp layer 0 input authors is null", tempPanelLayer0InputAuthors);
        Assert.assertEquals("Panel temp layer 0 input authors count is wrong", 1, tempPanelLayer0InputAuthors.size());
        Assert.assertEquals("Wrong temp layer 0 input author 0", "eReefs CSIRO GBR4 Hydrodynamic Model v2.0", tempPanelLayer0InputAuthors.get(0));
        Assert.assertEquals("Wrong temp layer 0 input licence", "CC-BY 4.0", tempPanelLayer0Input.getLicence());
        Assert.assertEquals("Wrong temp layer 0 input frame time increment unit", TimeIncrementUnit.HOUR, tempPanelLayer0Input.getTimeIncrement().getUnit());
        NcAnimateNetCDFVariableBean tempPanelLayer0Variable = tempPanelLayer0.getVariable();
        Assert.assertNotNull("Panel temp layer 0 variable is null", tempPanelLayer0Variable);
        Assert.assertEquals("Wrong temp layer 0 variable id", "ereefs/gbr4_v2/temp", tempPanelLayer0Variable.getId().getValue());
        Assert.assertEquals("Wrong temp layer 0 variable variableId", "temp", tempPanelLayer0Variable.getVariableId());
        Assert.assertEquals("Wrong temp layer 0 variable colourPaletteName", "x-Rainbow", tempPanelLayer0Variable.getColourPaletteName());
        Assert.assertEquals("Wrong temp layer 0 variable scaleMin", 22.0, tempPanelLayer0Variable.getScaleMin(), EPSILON);
        Assert.assertEquals("Wrong temp layer 0 variable scaleMax", 34.0, tempPanelLayer0Variable.getScaleMax(), EPSILON);

        NcAnimateLegendBean tempPanelLayer0VariableLegend = tempPanelLayer0Variable.getLegend();
        Assert.assertNotNull("Panel temp layer 0 variable legend is null", tempPanelLayer0VariableLegend);
        Assert.assertEquals("Wrong temp layer 0 variable legend colour band width", new Integer(20), tempPanelLayer0VariableLegend.getColourBandWidth());
        Assert.assertEquals("Wrong temp layer 0 variable legend colour band height", new Integer(300), tempPanelLayer0VariableLegend.getColourBandHeight());

        NcAnimatePositionBean tempPanelLayer0VariableLegendPosition = tempPanelLayer0VariableLegend.getPosition();
        Assert.assertNotNull("Wrong temp layer 0 variable legend position is null", tempPanelLayer0VariableLegendPosition);
        Assert.assertNull("Panel temp layer 0 variable legend position top is not null", tempPanelLayer0VariableLegendPosition.getTop());
        Assert.assertEquals("Wrong temp layer 0 variable legend position bottom", new Integer(5), tempPanelLayer0VariableLegendPosition.getBottom());
        Assert.assertEquals("Wrong temp layer 0 variable legend position left", new Integer(5), tempPanelLayer0VariableLegendPosition.getLeft());
        Assert.assertNull("Panel temp layer 0 variable legend position right is not null", tempPanelLayer0VariableLegendPosition.getRight());

        NcAnimateTextBean tempPanelLayer0VariableLegendTitle = tempPanelLayer0VariableLegend.getTitle();
        Assert.assertNotNull("Panel temp layer 0 variable legend title is null", tempPanelLayer0VariableLegendTitle);
        Assert.assertEquals("Wrong temp layer 0 variable legend title text", "Temperature", tempPanelLayer0VariableLegendTitle.getText().get(0));



        NcAnimateLayerBean tempPanelLayer1 = tempPanelLayers.get(1);
        Assert.assertNotNull("Panel temp layer 1 is null", tempPanelLayer1);
        Assert.assertEquals("Wrong temp layer 1 id", "ereefs-model_gbr4-bgc_924", tempPanelLayer1.getId().getValue());

        NcAnimateLayerBean tempPanelLayer2 = tempPanelLayers.get(2);
        Assert.assertNotNull("Panel temp layer 2 is null", tempPanelLayer2);
        Assert.assertEquals("Wrong temp layer 2 id", "world", tempPanelLayer2.getId().getValue());

        NcAnimateLayerBean tempPanelLayer3 = tempPanelLayers.get(3);
        Assert.assertNotNull("Panel temp layer 3 is null", tempPanelLayer3);
        Assert.assertEquals("Wrong temp layer 3 id", "australia", tempPanelLayer3.getId().getValue());

        NcAnimateLayerBean tempPanelLayer4 = tempPanelLayers.get(4);
        Assert.assertNotNull("Panel temp layer 4 is null", tempPanelLayer4);
        Assert.assertEquals("Wrong temp layer 4 id", "reefs", tempPanelLayer4.getId().getValue());

        NcAnimateLayerBean tempPanelLayer5 = tempPanelLayers.get(5);
        Assert.assertNotNull("Panel temp layer 5 is null", tempPanelLayer5);
        Assert.assertEquals("Wrong temp layer 5 id", "coralSea", tempPanelLayer5.getId().getValue());

        NcAnimateLayerBean tempPanelLayer6 = tempPanelLayers.get(6);
        Assert.assertNotNull("Panel temp layer 6 is null", tempPanelLayer6);
        Assert.assertEquals("Wrong temp layer 6 id", "catchments", tempPanelLayer6.getId().getValue());

        NcAnimateLayerBean tempPanelLayer7 = tempPanelLayers.get(7);
        Assert.assertNotNull("Panel temp layer 7 is null", tempPanelLayer7);
        Assert.assertEquals("Wrong temp layer 7 id", "GBRMPA_Bounds", tempPanelLayer7.getId().getValue());

        NcAnimateLayerBean tempPanelLayer8 = tempPanelLayers.get(8);
        Assert.assertNotNull("Panel temp layer 8 is null", tempPanelLayer8);
        Assert.assertEquals("Wrong temp layer 8 id", "rivers", tempPanelLayer8.getId().getValue());

        NcAnimateLayerBean tempPanelLayer9 = tempPanelLayers.get(9);
        Assert.assertNotNull("Panel temp layer 9 is null", tempPanelLayer9);
        Assert.assertEquals("Wrong temp layer 9 id", "cities", tempPanelLayer9.getId().getValue());


        // Wind panel
        NcAnimatePanelBean windPanel = panels.get(1);
        Assert.assertNotNull("Panel wind is null", windPanel);

        List<NcAnimateLayerBean> windPanelLayers = windPanel.getLayers();
        Assert.assertNotNull("Panel wind layers is null", windPanelLayers);
        Assert.assertEquals("Wrong number of panel wind layers", 10, windPanelLayers.size());

        NcAnimateLayerBean windPanelLayer0 = windPanelLayers.get(0);
        Assert.assertNotNull("Panel wind layer 0 is null", windPanelLayer0);
        Assert.assertEquals("Wrong wind layer 0 id", "ereefs-model_gbr4-v2", windPanelLayer0.getId().getValue());
        Assert.assertEquals("Wrong wind layer 0 type", NcAnimateLayerBean.LayerType.NETCDF, windPanelLayer0.getType());
        Assert.assertNull("Wrong wind layer 0 targetHeight", windPanelLayer0.getTargetHeight());
        Assert.assertNull("Wind layer 0 arrowSize is not null", windPanelLayer0.getArrowSize());
        NcAnimateInputBean windPanelLayer0Input = windPanelLayer0.getInput();
        Assert.assertNotNull("Panel wind layer 0 input is null", windPanelLayer0Input);
        Assert.assertEquals("Wrong wind layer 0 input id", "downloads/gbr4_v2", windPanelLayer0Input.getId().getValue());
        List<String> windPanelLayer0InputAuthors = windPanelLayer0Input.getAuthors();
        Assert.assertNotNull("Panel wind layer 0 input authors is null", windPanelLayer0InputAuthors);
        Assert.assertEquals("Panel wind layer 0 input authors count is wrong", 1, windPanelLayer0InputAuthors.size());
        Assert.assertEquals("Wrong wind layer 0 input author 0", "eReefs CSIRO GBR4 Hydrodynamic Model v2.0", windPanelLayer0InputAuthors.get(0));
        Assert.assertEquals("Wrong wind layer 0 input licence", "CC-BY 4.0", windPanelLayer0Input.getLicence());
        Assert.assertEquals("Wrong wind layer 0 input frame time increment unit", TimeIncrementUnit.HOUR, windPanelLayer0Input.getTimeIncrement().getUnit());
        NcAnimateNetCDFVariableBean windPanelLayer0Variable = windPanelLayer0.getVariable();
        Assert.assertNotNull("Panel wind layer 0 variable is null", windPanelLayer0Variable);
        Assert.assertNull("Wrong wind layer 0 variable id", windPanelLayer0Variable.getId().getValue());
        Assert.assertEquals("Wrong wind layer 0 variable variableId", "wspeed_u:wspeed_v-group", windPanelLayer0Variable.getVariableId());
        Assert.assertEquals("Wrong wind layer 0 variable colourPaletteName", "x-Rainbow", windPanelLayer0Variable.getColourPaletteName());
        Assert.assertEquals("Wrong wind layer 0 variable scaleMin", 0.0, windPanelLayer0Variable.getScaleMin(), EPSILON);
        Assert.assertEquals("Wrong wind layer 0 variable scaleMax", 20.0, windPanelLayer0Variable.getScaleMax(), EPSILON);

        NcAnimateLegendBean windPanelLayer0VariableLegend = windPanelLayer0Variable.getLegend();
        Assert.assertNotNull("Panel wind layer 0 variable legend is null", windPanelLayer0VariableLegend);
        Assert.assertEquals("Wrong wind layer 0 variable legend id", "bottom-left-legend", windPanelLayer0VariableLegend.getId().getValue());
        Assert.assertEquals("Wrong wind layer 0 variable legend colourBandWidth", new Integer(20), windPanelLayer0VariableLegend.getColourBandWidth());
        Assert.assertEquals("Wrong wind layer 0 variable legend colourBandWidth", new Integer(300), windPanelLayer0VariableLegend.getColourBandHeight());

        NcAnimatePositionBean windPanelLayer0VariableLegendPosition = windPanelLayer0VariableLegend.getPosition();
        Assert.assertNotNull("Panel wind layer 0 variable legend position is null", windPanelLayer0VariableLegendPosition);
        Assert.assertEquals("Wrong wind layer 0 variable legend position left", new Integer(5), windPanelLayer0VariableLegendPosition.getLeft());
        Assert.assertEquals("Wrong wind layer 0 variable legend position bottom", new Integer(5), windPanelLayer0VariableLegendPosition.getBottom());

        NcAnimateTextBean windPanelLayer0VariableLegendTitle = windPanelLayer0VariableLegend.getTitle();
        Assert.assertNotNull("Panel wind layer 0 variable legend title is null", windPanelLayer0VariableLegendTitle);
        Assert.assertEquals("Wrong wind layer 0 variable legend title text", "Wind speed", windPanelLayer0VariableLegendTitle.getText().get(0));
        Assert.assertEquals("Wrong wind layer 0 variable legend title fontSize", new Integer(16), windPanelLayer0VariableLegendTitle.getFontSize());
        Assert.assertEquals("Wrong wind layer 0 variable legend title fontColour", "#000000", windPanelLayer0VariableLegendTitle.getFontColour());



        // Salinity panel
        NcAnimatePanelBean saltPanel = panels.get(2);
        Assert.assertNotNull("Panel salt is null", saltPanel);

        NcAnimateTextBean saltPanelTitle = saltPanel.getTitle();
        Assert.assertNotNull("Panel salt title is null", saltPanelTitle);
        Assert.assertEquals("Wrong panel salt title fontSize", new Integer(30), saltPanelTitle.getFontSize());

        NcAnimatePositionBean saltPanelTitlePosition = saltPanelTitle.getPosition();
        Assert.assertNotNull("Panel salt title position is null", saltPanelTitlePosition);
        Assert.assertEquals("Wrong panel salt title position top", new Integer(35), saltPanelTitlePosition.getTop());
        Assert.assertNull("Wrong panel salt title position right", saltPanelTitlePosition.getRight());
        Assert.assertNull("Wrong panel salt title position bottom", saltPanelTitlePosition.getBottom());
        Assert.assertEquals("Wrong panel salt title position left", new Integer(25), saltPanelTitlePosition.getLeft());

        Assert.assertNull("Panel salt title bold is not null", saltPanelTitle.getBold());
        Assert.assertNull("Panel salt title italic is not null", saltPanelTitle.getItalic());

        List<String> saltPanelTitleTexts = saltPanelTitle.getText();
        Assert.assertNotNull("Panel salt title texts is null", saltPanelTitleTexts);
        Assert.assertEquals("Wrong number of panel salt title texts", "Salinity ${ctx.targetHeight %.1f}m", saltPanelTitleTexts.get(0));

        List<NcAnimateLayerBean> saltPanelLayers = saltPanel.getLayers();
        Assert.assertNotNull("Panel salt layers is null", saltPanelLayers);
        Assert.assertEquals("Wrong number of panel salt layers", 9, saltPanelLayers.size());

        NcAnimateLayerBean saltPanelLayer0 = saltPanelLayers.get(0);
        Assert.assertNotNull("Panel salt layer 0 is null", saltPanelLayer0);
        Assert.assertEquals("Wrong salt layer 0 id", "world", saltPanelLayer0.getId().getValue());

        NcAnimateLayerBean saltPanelLayer1 = saltPanelLayers.get(1);
        Assert.assertNotNull("Panel salt layer 1 is null", saltPanelLayer1);
        Assert.assertEquals("Wrong salt layer 1 id", "australia", saltPanelLayer1.getId().getValue());


        NcAnimateLayerBean saltPanelLayer2 = saltPanelLayers.get(2);
        Assert.assertNotNull("Panel salt layer 2 is null", saltPanelLayer2);
        Assert.assertEquals("Wrong salt layer 2 id", "ereefs-model_gbr4-v2", saltPanelLayer2.getId().getValue());
        Assert.assertEquals("Wrong salt layer 2 type", NcAnimateLayerBean.LayerType.NETCDF, saltPanelLayer2.getType());
        Assert.assertEquals("Wrong salt layer 2 targetHeight", "${ctx.targetHeight}", saltPanelLayer2.getTargetHeight());
        Assert.assertNull("Salt layer 2 arrowSize is not null", saltPanelLayer2.getArrowSize());
        NcAnimateInputBean saltPanelLayer2Input = saltPanelLayer2.getInput();
        Assert.assertNotNull("Panel salt layer 2 input is null", saltPanelLayer2Input);
        Assert.assertEquals("Wrong salt layer 2 input id", "downloads/gbr4_v2", saltPanelLayer2Input.getId().getValue());
        List<String> saltPanelLayer2InputAuthors = saltPanelLayer2Input.getAuthors();
        Assert.assertNotNull("Panel salt layer 2 input authors is null", saltPanelLayer2InputAuthors);
        Assert.assertEquals("Panel salt layer 2 input authors count is wrong", 1, saltPanelLayer2InputAuthors.size());
        Assert.assertEquals("Wrong salt layer 2 input author 0", "eReefs CSIRO GBR4 Hydrodynamic Model v2.0", saltPanelLayer2InputAuthors.get(0));
        Assert.assertEquals("Wrong salt layer 2 input licence", "CC-BY 4.0", saltPanelLayer2Input.getLicence());
        Assert.assertEquals("Wrong salt layer 2 input frame time increment unit", TimeIncrementUnit.HOUR, saltPanelLayer2Input.getTimeIncrement().getUnit());
        NcAnimateNetCDFVariableBean saltPanelLayer2Variable = saltPanelLayer2.getVariable();
        Assert.assertNotNull("Panel salt layer 2 variable is null", saltPanelLayer2Variable);
        Assert.assertEquals("Wrong salt layer 2 variable id", "ereefs/gbr4_v2/salt", saltPanelLayer2Variable.getId().getValue());
        Assert.assertEquals("Wrong salt layer 2 variable variableId", "salt", saltPanelLayer2Variable.getVariableId());
        Assert.assertEquals("Wrong salt layer 2 variable colourPaletteName", "x-Rainbow-inv", saltPanelLayer2Variable.getColourPaletteName());
        Assert.assertEquals("Wrong salt layer 2 variable scaleMin", 2.0, saltPanelLayer2Variable.getScaleMin(), EPSILON);
        Assert.assertEquals("Wrong salt layer 2 variable scaleMax", 100.0, saltPanelLayer2Variable.getScaleMax(), EPSILON);

        NcAnimateLegendBean saltPanelLayer2VariableLegend = saltPanelLayer2Variable.getLegend();
        Assert.assertNotNull("Panel salt layer 2 variable legend is null", saltPanelLayer2VariableLegend);
        Assert.assertEquals("Wrong salt layer 2 variable legend colour band width", new Integer(20), saltPanelLayer2VariableLegend.getColourBandWidth());
        Assert.assertEquals("Wrong salt layer 2 variable legend colour band height", new Integer(300), saltPanelLayer2VariableLegend.getColourBandHeight());

        NcAnimatePositionBean saltPanelLayer2VariableLegendPosition = saltPanelLayer2VariableLegend.getPosition();
        Assert.assertNotNull("Wrong salt layer 2 variable legend position is null", saltPanelLayer2VariableLegendPosition);
        Assert.assertNull("Panel salt layer 2 variable legend position top is not null", saltPanelLayer2VariableLegendPosition.getTop());
        Assert.assertEquals("Wrong salt layer 2 variable legend position bottom", new Integer(10), saltPanelLayer2VariableLegendPosition.getBottom());
        Assert.assertNull("Panel salt layer 2 variable legend position left is not null", saltPanelLayer2VariableLegendPosition.getLeft());
        Assert.assertEquals("Wrong salt layer 2 variable legend position right", new Integer(20), saltPanelLayer2VariableLegendPosition.getRight());

        NcAnimateTextBean saltPanelLayer2VariableLegendTitle = saltPanelLayer2VariableLegend.getTitle();
        Assert.assertNotNull("Panel salt layer 2 variable legend title is null", saltPanelLayer2VariableLegendTitle);
        Assert.assertEquals("Wrong salt layer 2 variable legend title text", "Salinity", saltPanelLayer2VariableLegendTitle.getText().get(0));



        NcAnimateLayerBean saltPanelLayer3 = saltPanelLayers.get(3);
        Assert.assertNotNull("Panel salt layer 3 is null", saltPanelLayer3);
        Assert.assertEquals("Wrong salt layer 3 id", "reefs", saltPanelLayer3.getId().getValue());

        NcAnimateLayerBean saltPanelLayer4 = saltPanelLayers.get(4);
        Assert.assertNotNull("Panel salt layer 4 is null", saltPanelLayer4);
        Assert.assertEquals("Wrong salt layer 4 id", "coralSea", saltPanelLayer4.getId().getValue());

        NcAnimateLayerBean saltPanelLayer5 = saltPanelLayers.get(5);
        Assert.assertNotNull("Panel salt layer 5 is null", saltPanelLayer5);
        Assert.assertEquals("Wrong salt layer 5 id", "catchments", saltPanelLayer5.getId().getValue());

        NcAnimateLayerBean saltPanelLayer6 = saltPanelLayers.get(6);
        Assert.assertNotNull("Panel salt layer 6 is null", saltPanelLayer6);
        Assert.assertEquals("Wrong salt layer 6 id", "GBRMPA_Bounds", saltPanelLayer6.getId().getValue());

        NcAnimateLayerBean saltPanelLayer7 = saltPanelLayers.get(7);
        Assert.assertNotNull("Panel salt layer 7 is null", saltPanelLayer7);
        Assert.assertEquals("Wrong salt layer 7 id", "rivers", saltPanelLayer7.getId().getValue());

        NcAnimateLayerBean saltPanelLayer8 = saltPanelLayers.get(8);
        Assert.assertNotNull("Panel salt layer 8 is null", saltPanelLayer8);
        Assert.assertEquals("Wrong salt layer 8 id", "cities", saltPanelLayer8.getId().getValue());


        // Current panel (as in water current)
        NcAnimatePanelBean currentPanel = panels.get(3);
        Assert.assertNotNull("Panel current is null", currentPanel);

        List<NcAnimateLayerBean> currentPanelLayers = currentPanel.getLayers();
        Assert.assertNotNull("Panel current layers is null", currentPanelLayers);
        Assert.assertEquals("Wrong number of panel current layers", 10, currentPanelLayers.size());

        NcAnimateLayerBean currentPanelLayer0 = currentPanelLayers.get(0);
        Assert.assertNotNull("Panel current layer 0 is null", currentPanelLayer0);
        Assert.assertEquals("Wrong current layer 0 variable arrowSize", new Integer(10), currentPanelLayer0.getArrowSize());

        NcAnimateNetCDFVariableBean currentPanelLayer0Variable = currentPanelLayer0.getVariable();
        Assert.assertNotNull("Panel current layer 0 variable is null", currentPanelLayer0Variable);
        Assert.assertEquals("Wrong current layer 0 variable id", "ereefs/gbr4_v2/current", currentPanelLayer0Variable.getId().getValue());
        Assert.assertEquals("Wrong current layer 0 variable variableId", "u:v-group", currentPanelLayer0Variable.getVariableId());
        Assert.assertEquals("Wrong current layer 0 variable colourPaletteName", "aims-BrightRainbow", currentPanelLayer0Variable.getColourPaletteName());
        Assert.assertEquals("Wrong current layer 0 variable scaleMin", 0.0, currentPanelLayer0Variable.getScaleMin(), EPSILON);
        Assert.assertEquals("Wrong current layer 0 variable scaleMax", 0.8, currentPanelLayer0Variable.getScaleMax(), EPSILON);

        NcAnimateLegendBean currentPanelLayer0VariableLegend = currentPanelLayer0Variable.getLegend();
        Assert.assertNotNull("Panel current layer 0 variable legend is null", currentPanelLayer0VariableLegend);
        Assert.assertEquals("Wrong current layer 0 variable legend colour band width", new Integer(20), currentPanelLayer0VariableLegend.getColourBandWidth());
        Assert.assertEquals("Wrong current layer 0 variable legend colour band height", new Integer(300), currentPanelLayer0VariableLegend.getColourBandHeight());

        NcAnimatePositionBean currentPanelLayer0VariableLegendPosition = currentPanelLayer0VariableLegend.getPosition();
        Assert.assertNotNull("Wrong current layer 0 variable legend position is null", currentPanelLayer0VariableLegendPosition);
        Assert.assertEquals("Wrong current layer 0 variable legend position top", new Integer(5), currentPanelLayer0VariableLegendPosition.getTop());
        Assert.assertNull("Panel current layer 0 variable legend position bottom is not null", currentPanelLayer0VariableLegendPosition.getBottom());
        Assert.assertNull("Panel current layer 0 variable legend position left is not null", currentPanelLayer0VariableLegendPosition.getLeft());
        Assert.assertEquals("Wrong current layer 0 variable legend position right", new Integer(5), currentPanelLayer0VariableLegendPosition.getRight());

        NcAnimateTextBean currentPanelLayer0VariableLegendTitle = currentPanelLayer0VariableLegend.getTitle();
        Assert.assertNotNull("Panel current layer 0 variable legend title is null", currentPanelLayer0VariableLegendTitle);
        Assert.assertEquals("Wrong current layer 0 variable legend title text", "Current [ms-1]", currentPanelLayer0VariableLegendTitle.getText().get(0));


        // True colour panel
        NcAnimatePanelBean trueColourPanel = panels.get(4);
        Assert.assertNotNull("Panel true colour is null", trueColourPanel);

        List<NcAnimateLayerBean> trueColourPanelLayers = trueColourPanel.getLayers();
        Assert.assertNotNull("Panel true colour layers is null", trueColourPanelLayers);
        Assert.assertEquals("Wrong number of panel true colour layers", 10, trueColourPanelLayers.size());

        NcAnimateLayerBean trueColourPanelLayer1 = trueColourPanelLayers.get(1);
        Assert.assertNotNull("Panel true colour layer 1 is null", trueColourPanelLayer1);

        Map<String, NcAnimateNetCDFTrueColourVariableBean> trueColourPanelLayer1ColourVars = trueColourPanelLayer1.getTrueColourVariables();
        Assert.assertNotNull("Panel true colour layer 1 colour variables is null", trueColourPanelLayer1ColourVars);

        Assert.assertEquals("Wrong true colour layer 1 variable arrowSize", 3, trueColourPanelLayer1ColourVars.size());

        NcAnimateNetCDFTrueColourVariableBean trueColourPanelLayer1ColourVarR470 = trueColourPanelLayer1ColourVars.get("R_470");
        NcAnimateNetCDFTrueColourVariableBean trueColourPanelLayer1ColourVarR555 = trueColourPanelLayer1ColourVars.get("R_555");
        NcAnimateNetCDFTrueColourVariableBean trueColourPanelLayer1ColourVarR645 = trueColourPanelLayer1ColourVars.get("R_645");

        Assert.assertNotNull("Panel true colour layer 1 colour variables R_470 is null", trueColourPanelLayer1ColourVarR470);
        Assert.assertNotNull("Panel true colour layer 1 colour variables R_555 is null", trueColourPanelLayer1ColourVarR555);
        Assert.assertNotNull("Panel true colour layer 1 colour variables R_645 is null", trueColourPanelLayer1ColourVarR645);

        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_470 hex colours",
            new ArrayList<String>(Arrays.asList(
                "#000001",
                "#00005e",
                "#000091",
                "#0000ae",
                "#0000c3",
                "#0000d5",
                "#0000e0",
                "#0000eb",
                "#0000f3",
                "#0000f9",
                "#0000ff"
            )),
            trueColourPanelLayer1ColourVarR470.getHexColours());
        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_470 scale min", new Float(0), trueColourPanelLayer1ColourVarR470.getScaleMin());
        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_470 scale max", new Float(0.1), trueColourPanelLayer1ColourVarR470.getScaleMax());

        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_555 hex colours",
            new ArrayList<String>(Arrays.asList(
                "#000100",
                "#005e00",
                "#009100",
                "#00ae00",
                "#00c300",
                "#00d500",
                "#00e000",
                "#00eb00",
                "#00f300",
                "#00f900",
                "#00ff00"
            )),
            trueColourPanelLayer1ColourVarR555.getHexColours());
        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_555 scale min", new Float(0), trueColourPanelLayer1ColourVarR555.getScaleMin());
        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_555 scale max", new Float(0.1), trueColourPanelLayer1ColourVarR555.getScaleMax());

        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_645 hex colours",
            new ArrayList<String>(Arrays.asList(
                "#010000",
                "#5e0000",
                "#910000",
                "#ae0000",
                "#c30000",
                "#d50000",
                "#e00000",
                "#eb0000",
                "#f30000",
                "#f90000",
                "#ff0000"
            )),
            trueColourPanelLayer1ColourVarR645.getHexColours());
        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_645 scale min", new Float(0), trueColourPanelLayer1ColourVarR645.getScaleMin());
        Assert.assertEquals("Wrong panel true colour layer 1 colour variables R_645 scale max", new Float(0.1), trueColourPanelLayer1ColourVarR645.getScaleMax());


        // ************
        // ** Render **
        // ************

        NcAnimateRenderBean render = config.getRender();
        Assert.assertNotNull("Render is null", render);

        Assert.assertEquals("Render ID is wrong", "default-render", render.getId().getValue());

        Map<String, NcAnimateRenderMapBean> maps = render.getMaps();
        Assert.assertNotNull("Render maps is null", maps);
        Assert.assertEquals("Wrong number of render maps", 2, maps.size());

        Map<String, NcAnimateRenderVideoBean> videos = render.getVideos();
        Assert.assertNotNull("Render videos is null", videos);
        Assert.assertEquals("Wrong number of render videos", 2, videos.size());

        NcAnimateRenderVideoBean wmvVideo = videos.get("wmvVideo");
        Assert.assertNotNull("Render WMV video is null", wmvVideo);
        Assert.assertEquals("Wrong FPS for render WMV video", new Integer(10), wmvVideo.getFps());
        Assert.assertNull("Render WMV video block size is not null", wmvVideo.getBlockSize());

        NcAnimateRenderVideoBean mp4Video = videos.get("mp4Video");
        Assert.assertNotNull("Render MP4 video is null", mp4Video);
        Assert.assertEquals("Wrong FPS for render MP4 video", new Integer(12), mp4Video.getFps());
        Integer[] mp4VideoBlockSize = mp4Video.getBlockSize();
        Assert.assertNotNull("Render MP4 video block size is null", mp4VideoBlockSize);
        Assert.assertEquals("Wrong number of block size for render MP4 video", 2, mp4VideoBlockSize.length);
        Assert.assertEquals("Wrong first block size for render MP4 video", new Integer(16), mp4VideoBlockSize[0]);
        Assert.assertEquals("Wrong second block size for render MP4 video", new Integer(8), mp4VideoBlockSize[1]);


        // Metadata
        /*
        "metadata": {
            "properties": {
                "region": {
                    "id": "${ctx.region.id}",
                    "label": "${ctx.region.label}",
                    "bbox": {
                        "east": "${ctx.region.bbox.east}",
                        "north": "${ctx.region.bbox.north}",
                        "south": "${ctx.region.bbox.south}",
                        "west": "${ctx.region.bbox.west}"
                    }
                },
                "targetHeight": "${ctx.targetHeight}",
                "framePeriod": "${ctx.framePeriod}",

                "testArray": [
                    "value1",
                    "value2",
                    [
                        "value3.1",
                        "value3.2",
                        {
                            "id": "value3.3",
                            "nestedArrayValue": [
                                "value3.3.1",
                                "value3.3.2"
                            ],
                            "nestedObjectValue": {
                                "id": "value3.3.3"
                            }
                        }
                    ],
                    {
                        "id": "value4",
                        "nestedArrayValue": [
                            "value4.1",
                            "value4.2"
                        ],
                        "nestedObjectValue": {
                            "id": "value4.3"
                        }
                    }
                ]
            }
        }
        */

        NcAnimateRenderMetadataBean metadata = render.getMetadata();
        Assert.assertNotNull("Render metadata is null", metadata);

        Map<String, Object> metadataProperties = metadata.getProperties();
        Assert.assertNotNull("Render metadata properties is null", metadataProperties);

        Object metadataPropertyTargetHeight = metadataProperties.get("targetHeight");
        Assert.assertTrue("Render metadata property targetHeight is not a String", (metadataPropertyTargetHeight instanceof String));
        Assert.assertEquals("Render metadata property targetHeight is wrong", "${ctx.targetHeight}", metadataPropertyTargetHeight);

        Object metadataPropertyFramePeriod = metadataProperties.get("framePeriod");
        Assert.assertTrue("Render metadata property framePeriod is not a String", (metadataPropertyFramePeriod instanceof String));
        Assert.assertEquals("Render metadata property framePeriod is wrong", "${ctx.framePeriod}", metadataPropertyFramePeriod);

        Object rawMetadataPropertyRegion = metadataProperties.get("region");
        Assert.assertTrue("Render metadata property region is not a Map", (rawMetadataPropertyRegion instanceof Map));
        Map<String, Object> metadataPropertyRegion = (Map<String, Object>)rawMetadataPropertyRegion;

        Object metadataPropertyRegionId = metadataPropertyRegion.get("id");
        Assert.assertTrue("Render metadata property region ID is not a String", (metadataPropertyRegionId instanceof String));
        Assert.assertEquals("Render metadata property region ID is wrong", "${ctx.region.id}", metadataPropertyRegionId);

        Object metadataPropertyRegionLabel = metadataPropertyRegion.get("label");
        Assert.assertTrue("Render metadata property region ID is not a String", (metadataPropertyRegionLabel instanceof String));
        Assert.assertEquals("Render metadata property region ID is wrong", "${ctx.region.label}", metadataPropertyRegionLabel);

        Object rawMetadataPropertyRegionBbox = metadataPropertyRegion.get("bbox");
        Assert.assertTrue("Render metadata property region bbox is not a Map", (rawMetadataPropertyRegionBbox instanceof Map));
        Map<String, Object> metadataPropertyRegionBbox = (Map<String, Object>)rawMetadataPropertyRegionBbox;

        Object metadataPropertyRegionBboxNorth = metadataPropertyRegionBbox.get("north");
        Assert.assertTrue("Render metadata property region bbox north is not a String", (metadataPropertyRegionBboxNorth instanceof String));
        Assert.assertEquals("Render metadata property region bbox north is wrong", "${ctx.region.bbox.north}", metadataPropertyRegionBboxNorth);

        Object metadataPropertyRegionBboxEast = metadataPropertyRegionBbox.get("east");
        Assert.assertTrue("Render metadata property region bbox east is not a String", (metadataPropertyRegionBboxEast instanceof String));
        Assert.assertEquals("Render metadata property region bbox east is wrong", "${ctx.region.bbox.east}", metadataPropertyRegionBboxEast);

        Object metadataPropertyRegionBboxSouth = metadataPropertyRegionBbox.get("south");
        Assert.assertTrue("Render metadata property region bbox south is not a String", (metadataPropertyRegionBboxSouth instanceof String));
        Assert.assertEquals("Render metadata property region bbox south is wrong", "${ctx.region.bbox.south}", metadataPropertyRegionBboxSouth);

        Object metadataPropertyRegionBboxWest = metadataPropertyRegionBbox.get("west");
        Assert.assertTrue("Render metadata property region bbox west is not a String", (metadataPropertyRegionBboxWest instanceof String));
        Assert.assertEquals("Render metadata property region bbox west is wrong", "${ctx.region.bbox.west}", metadataPropertyRegionBboxWest);


        Object rawMetadataPropertyTestArray = metadataProperties.get("testArray");
        Assert.assertTrue("Render metadata property testArray is not a List", (rawMetadataPropertyTestArray instanceof List));
        List<Object> metadataPropertyTestArray = (List<Object>)rawMetadataPropertyTestArray;

        for (int i=0; i<metadataPropertyTestArray.size(); i++) {
            Object metadataPropertyTestArrayItem = metadataPropertyTestArray.get(i);

            switch(i) {
                case 0:
                    Assert.assertTrue("Render metadata property testArray[0] is not a String", (metadataPropertyTestArrayItem instanceof String));
                    Assert.assertEquals("Render metadata property testArray[0] is wrong", "value1", metadataPropertyTestArrayItem);
                    break;

                case 1:
                    Assert.assertTrue("Render metadata property testArray[1] is not a String", (metadataPropertyTestArrayItem instanceof String));
                    Assert.assertEquals("Render metadata property testArray[1] is wrong", "value2", metadataPropertyTestArrayItem);
                    break;

                case 2:
                    Assert.assertTrue("Render metadata property testArray[2] is not a List", (metadataPropertyTestArrayItem instanceof List));
                    List<Object> metadataPropertyTestArrayItemArray = (List<Object>)metadataPropertyTestArrayItem;
                    for (int j=0; j<metadataPropertyTestArrayItemArray.size(); j++) {
                        Object metadataPropertyTestArrayItemArrayItem = metadataPropertyTestArrayItemArray.get(j);
                        switch(j) {
                            case 0:
                                Assert.assertTrue("Render metadata property testArray[2][0] is not a String", (metadataPropertyTestArrayItemArrayItem instanceof String));
                                Assert.assertEquals("Render metadata property testArray[2][0] is wrong", "value3.1", metadataPropertyTestArrayItemArrayItem);
                                break;

                            case 1:
                                Assert.assertTrue("Render metadata property testArray[2][1] is not a String", (metadataPropertyTestArrayItemArrayItem instanceof String));
                                Assert.assertEquals("Render metadata property testArray[2][1] is wrong", "value3.2", metadataPropertyTestArrayItemArrayItem);
                                break;

                            case 2:
                                Assert.assertTrue("Render metadata property testArray[2][2] is not a Map", (metadataPropertyTestArrayItemArrayItem instanceof Map));
                                Map<String, Object> metadataPropertyTestArrayItemArrayItemMap = (Map<String, Object>)metadataPropertyTestArrayItemArrayItem;

                                Object metadataPropertyTestArrayItemArrayItemMapId = metadataPropertyTestArrayItemArrayItemMap.get("id");
                                Assert.assertTrue("Render metadata property testArray[2][2].id is not a String", (metadataPropertyTestArrayItemArrayItemMapId instanceof String));
                                Assert.assertEquals("Render metadata property testArray[2][2].id is wrong", "value3.3", metadataPropertyTestArrayItemArrayItemMapId);

                                Object rawMetadataPropertyTestArrayItemArrayItemMapMap = metadataPropertyTestArrayItemArrayItemMap.get("nestedObjectValue");
                                Assert.assertTrue("Render metadata property testArray[2][2].nestedObjectValue is not a Map", (rawMetadataPropertyTestArrayItemArrayItemMapMap instanceof Map));
                                Map<String, Object> metadataPropertyTestArrayItemArrayItemMapMap = (Map<String, Object>)rawMetadataPropertyTestArrayItemArrayItemMapMap;

                                Object rawMetadataPropertyTestArrayItemArrayItemMapArray = metadataPropertyTestArrayItemArrayItemMap.get("nestedArrayValue");
                                Assert.assertTrue("Render metadata property testArray[2][2].nestedArrayValue is not a List", (rawMetadataPropertyTestArrayItemArrayItemMapArray instanceof List));
                                List<Object> metadataPropertyTestArrayItemArrayItemMapArray = (List<Object>)rawMetadataPropertyTestArrayItemArrayItemMapArray;
                                for (int k=0; k<metadataPropertyTestArrayItemArrayItemMapArray.size(); k++) {
                                    Object metadataPropertyTestArrayItemArrayItemMapArrayItem = metadataPropertyTestArrayItemArrayItemMapArray.get(k);
                                    switch(k) {
                                        case 0:
                                            Assert.assertTrue("Render metadata property testArray[2][2].nestedArrayValue[0] is not a String", (metadataPropertyTestArrayItemArrayItemMapArrayItem instanceof String));
                                            Assert.assertEquals("Render metadata property testArray[2][2].nestedArrayValue[0] is wrong", "value3.3.1", metadataPropertyTestArrayItemArrayItemMapArrayItem);
                                            break;

                                        case 1:
                                            Assert.assertTrue("Render metadata property testArray[2][2].nestedArrayValue[1] is not a String", (metadataPropertyTestArrayItemArrayItemMapArrayItem instanceof String));
                                            Assert.assertEquals("Render metadata property testArray[2][2].nestedArrayValue[1] is wrong", "value3.3.2", metadataPropertyTestArrayItemArrayItemMapArrayItem);
                                            break;

                                        default:
                                            Assert.fail("Unexpected element found in render metadata property testArray[2][2].nestedArrayValue, index: " + k);
                                    }
                                }

                                Object metadataPropertyTestArrayItemArrayItemMapMapId = metadataPropertyTestArrayItemArrayItemMapMap.get("id");
                                Assert.assertTrue("Render metadata property testArray[2][2].nestedObjectValue.id is not a String", (metadataPropertyTestArrayItemArrayItemMapMapId instanceof String));
                                Assert.assertEquals("Render metadata property testArray[2][2].nestedObjectValue.id is wrong", "value3.3.3", metadataPropertyTestArrayItemArrayItemMapMapId);

                                break;

                            default:
                                Assert.fail("Unexpected element found in render metadata property testArray[2], index: " + j);
                        }
                    }
                    break;

                case 3:
                    Assert.assertTrue("Render metadata property testArray[3] is not a Map", (metadataPropertyTestArrayItem instanceof Map));
                    Map<String, Object> metadataPropertyTestArrayItemMap = (Map<String, Object>)metadataPropertyTestArrayItem;

                    Object metadataPropertyTestArrayItemMapId = metadataPropertyTestArrayItemMap.get("id");
                    Assert.assertTrue("Render metadata property testArray[3].id is not a String", (metadataPropertyTestArrayItemMapId instanceof String));
                    Assert.assertEquals("Render metadata property testArray[3].id is wrong", "value4", metadataPropertyTestArrayItemMapId);

                    Object rawMetadataPropertyTestArrayItemMapArray = metadataPropertyTestArrayItemMap.get("nestedArrayValue");
                    Assert.assertTrue("Render metadata property testArray[3].nestedArrayValue is not a List", (rawMetadataPropertyTestArrayItemMapArray instanceof List));
                    List<Object> metadataPropertyTestArrayItemMapArray = (List<Object>)rawMetadataPropertyTestArrayItemMapArray;

                    for (int j=0; j<metadataPropertyTestArrayItemMapArray.size(); j++) {
                        Object metadataPropertyTestArrayItemMapArrayItem = metadataPropertyTestArrayItemMapArray.get(j);
                        switch(j) {
                            case 0:
                                Assert.assertTrue("Render metadata property testArray[3].nestedArrayValue[0] is not a String", (metadataPropertyTestArrayItemMapArrayItem instanceof String));
                                Assert.assertEquals("Render metadata property testArray[3].nestedArrayValue[0] is wrong", "value4.1", metadataPropertyTestArrayItemMapArrayItem);
                                break;

                            case 1:
                                Assert.assertTrue("Render metadata property testArray[3].nestedArrayValue[1] is not a String", (metadataPropertyTestArrayItemMapArrayItem instanceof String));
                                Assert.assertEquals("Render metadata property testArray[3].nestedArrayValue[1] is wrong", "value4.2", metadataPropertyTestArrayItemMapArrayItem);
                                break;

                            default:
                                Assert.fail("Unexpected element found in render metadata property testArray[3].nestedArrayValue, index: " + j);
                        }
                    }

                    Object rawMetadataPropertyTestArrayItemMapMap = metadataPropertyTestArrayItemMap.get("nestedObjectValue");
                    Assert.assertTrue("Render metadata property testArray[3].nestedObjectValue is not a Map", (rawMetadataPropertyTestArrayItemMapMap instanceof Map));
                    Map<String, Object> metadataPropertyTestArrayItemMapMap = (Map<String, Object>)rawMetadataPropertyTestArrayItemMapMap;

                    Object metadataPropertyTestArrayItemMapMapId = metadataPropertyTestArrayItemMapMap.get("id");
                    Assert.assertTrue("Render metadata property testArray[3].nestedObjectValue.id is not a String", (metadataPropertyTestArrayItemMapMapId instanceof String));
                    Assert.assertEquals("Render metadata property testArray[3].nestedObjectValue.id is wrong", "value4.3", metadataPropertyTestArrayItemMapMapId);

                    break;

                default:
                    Assert.fail("Unexpected element found in render metadata property testArray, index: " + i);
            }
        }
    }
}
