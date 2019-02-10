package us.ilite.display;


import eu.hansolo.fx.charts.SankeyPlot;
import eu.hansolo.fx.charts.SankeyPlotBuilder;
import eu.hansolo.fx.charts.data.PlotItem;
import eu.hansolo.fx.regulators.FeedbackRegulator;
import eu.hansolo.fx.regulators.FeedbackRegulatorBuilder;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.addons.Indicator;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome.FontAwesome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DriverStation extends Application {
    private static final double sSCREEN_WIDTH = 1920;
    private static final double sSCREEN_HEIGHT = 750;
    private static final double sBUTTON_BAR_HEIGHT_PX = 80;
    private static final double sTILE_SIZE_HEIGHT_PX = (sSCREEN_HEIGHT-sBUTTON_BAR_HEIGHT_PX)/2;
    private static final double sTILE_SIZE_WIDTH_PX = sSCREEN_WIDTH/6d;

    @Override
    public void start(Stage stage) throws Exception {

        BorderPane root = new BorderPane();

        root.setLeft(createElevatorPane());
        root.setCenter(createPDPPane());

        Scene scene = new Scene(root, sSCREEN_WIDTH, sSCREEN_HEIGHT);
        stage.setOnCloseRequest(e -> {
            IliteCodexReceiver.getInstance().disconnect();
            System.exit(0);
        });
        stage.setScene(scene);
        stage.show();
        stage.setX(0);
        stage.setY(0);
    }

    private Stop[] mElevatorGradient = new Stop[] {
            new Stop(0.0, Color.BLACK),
            new Stop(29d/81d, Color.BLACK),
            new Stop(32d/81d, Color.LIMEGREEN),
            new Stop(35d/81d, Color.BLACK),
            new Stop(1.0, Color.BLACK)
    };


    private Pane createElevatorPane() {
        FeedbackRegulator elevatorPosition = FeedbackRegulatorBuilder.create()
                .minValue(0)
                .maxValue(81)
                .decimals(0)
                .textColor(Color.RED)
                .icon(FontAwesome.EXCLAMATION_TRIANGLE)
                .iconColor(Color.RED)
                .gradientStops(mElevatorGradient)
                .currentValue(31)
                .targetValue(51)
                .unit("")
                .build();

        Tile elevatorTile = TileBuilder.create()
                .title("Elevator Height")
                .prefSize(sTILE_SIZE_WIDTH_PX, sTILE_SIZE_HEIGHT_PX)
                .skinType(Tile.SkinType.CUSTOM)
                .graphic(elevatorPosition)
                .text("Target Position: Cargo 2")
                .textAlignment(TextAlignment.RIGHT)
                .textSize(Tile.TextSize.SMALLER)
                .build();

        VBox left = new VBox(elevatorTile);
        VBox right = new VBox();

        HBox columns = new HBox(left,right);
        return columns;
    }

    private final Map<String, PlotItem> mPlotItems = new HashMap<>();

    private Pane createPDPPane() {
        mPlotItems.put("Left Drive Train", new PlotItem());
        mPlotItems.put("Right Drive Train", new PlotItem());
        mPlotItems.put("Elevator", new PlotItem());
        mPlotItems.put("4-Bar Climber", new PlotItem());
        mPlotItems.put("Intake", new PlotItem());
        mPlotItems.put("Cargo Shooter", new PlotItem());
        mPlotItems.put("System", new PlotItem());

        PlotItem pdp = new PlotItem("Power Distribution Panel", Color.BLACK);
        for(PlotItem pi : mPlotItems.values() ) {
            pdp.addToOutgoing(pi, 30);
        }
        mPlotItems.put("Power Distribution Panel", pdp);


        for(String k : mPlotItems.keySet()) {
            mPlotItems.get(k).setName(k);
            mPlotItems.get(k).setValue(30d);
        }

        SankeyPlot pdpPlot = SankeyPlotBuilder.create()
                .items(new ArrayList<>(mPlotItems.values()))
                .showFlowDirection(true)
                .prefSize(2*sTILE_SIZE_WIDTH_PX, 2*sTILE_SIZE_HEIGHT_PX)
                .decimals(0)
                .useItemColor(true)
                .build();

        Tile pdpTile = TileBuilder.create()
                .skinType(Tile.SkinType.CUSTOM)
                .graphic(pdpPlot)
                .title("Power System")
                .prefSize(sTILE_SIZE_WIDTH_PX*2, sTILE_SIZE_HEIGHT_PX)
                .build();

        VBox left = new VBox(pdpTile);
        VBox right = new VBox();

        HBox columns = new HBox(left,right);
        return columns;
    }

    private Pane createDriveTrainPane() {
        Gauge throttleGauge = GaugeBuilder.create()
                .skinType(Gauge.SkinType.BAR)
                .minValue(0)
                .maxValue(100)
                .build();

        Tile turn = TileBuilder.create()
                .value(0d)
                .minValue(-100)
                .maxValue(100)
                .title("Turn")
                .skinType(Tile.SkinType.GAUGE)
                .fillWithGradient(false)
                .prefSize(sTILE_SIZE_WIDTH_PX,sTILE_SIZE_HEIGHT_PX)
                .build();

        Tile throttle = TileBuilder.create()
                .title("Throttle")
                .skinType(Tile.SkinType.CUSTOM)
                .graphic(throttleGauge)
                .prefSize(sTILE_SIZE_WIDTH_PX,sTILE_SIZE_HEIGHT_PX)
                .build();

        VBox left = new VBox(throttle);
        VBox right = new VBox(turn);

        HBox columns = new HBox(left,right);
        columns.setPrefHeight(2*sTILE_SIZE_HEIGHT_PX);
        columns.setPrefWidth(4*sTILE_SIZE_WIDTH_PX);
        columns.setPadding(new Insets(5));
        return columns;
    }

    private static HBox createIndicatorRow(final String pText, double prefWidth) {
        Indicator indicator = new Indicator(Tile.BLUE, Tile.GRAY);
        Label label     = new Label(pText);
        label.setFont(Font.font(18));
        label.setTextFill(Tile.FOREGROUND);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setPrefWidth(prefWidth);

        HBox box = new HBox(50, indicator, label);
        box.setAlignment(Pos.CENTER);

        indicator.setOnMousePressed(e -> indicator.setOn(!indicator.isOn()));

        return box;
    }

    public static void main(String[] pArgs) {
        // Start the receivers before we start the display
        IliteCodexReceiver.getInstance();

        // Start the display
        launch(pArgs);
    }
}
