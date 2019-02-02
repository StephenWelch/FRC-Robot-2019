package us.ilite.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flybotix.hfr.codex.Codex;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import us.ilite.common.io.CodexNetworkTables;
import us.ilite.common.io.CodexNetworkTablesParser;
import us.ilite.common.lib.util.SimpleNetworkTable;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.EDriverInputMode;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.common.types.EFourBarData;

public class Data {

    public CodexNetworkTables mCodexNT = CodexNetworkTables.getInstance();
    
    //Add new codexes here as we need more
    public Codex<Double, EGyro> imu = Codex.of.thisEnum(EGyro.class);
    public Codex<Double, EDriveData> drive = Codex.of.thisEnum(EDriveData.class);
    public Codex<Double, ELogitech310> driverinput = Codex.of.thisEnum(ELogitech310.class);
    public Codex<Double, ELogitech310> operatorinput = Codex.of.thisEnum(ELogitech310.class);
    public Codex<Double, EFourBarData> fourbar = Codex.of.thisEnum(EFourBarData.class);

    public static NetworkTableInstance kInst = NetworkTableInstance.getDefault();
    public static SimpleNetworkTable kLoggingTable = new SimpleNetworkTable("LoggingTable");
    public static SimpleNetworkTable kSmartDashboard = new SimpleNetworkTable("SmartDashboard");
    public static NetworkTable kLimelight = kInst.getTable("limelight");
    public static SimpleNetworkTable kDriverControlSelection = new SimpleNetworkTable("DriverControlSelection") {
        @Override
        public void initKeys() {
            getInstance().getEntry(EDriverInputMode.class.getSimpleName()).setDefaultNumber(-1);
        }
    };

    //Stores writers per codex needed for CSV logging
    private Map<String, Writer> mWriters = new HashMap<String, Writer>();

    private List<CodexNetworkTablesParser> mLoggedCodexes;

    public Data() {
        //Add new codexes as we support more into this list
        mLoggedCodexes = Arrays.asList(
            new CodexNetworkTablesParser<EGyro>(imu),
            new CodexNetworkTablesParser<EDriveData>(drive),
            new CodexNetworkTablesParser<ELogitech310>(driverinput, "DRIVER"),
            new CodexNetworkTablesParser<ELogitech310>(operatorinput, "OPERATOR"),
            new CodexNetworkTablesParser<EFourBarData>(fourbar)
        );

        //This loop makes a Writer for each parser and sticks it into mWriters
        for (CodexNetworkTablesParser parser : mLoggedCodexes) {
            try {
                File file = parser.file();
                handleCreation(file);
                mWriters.put(parser.getCSVIdentifier(), new BufferedWriter(new FileWriter(parser.file())));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Translate NT to on-computer codex for each CodexNetworkTablesParser in mLoggedCodexes
     */
    public void logFromNetworkTables() {
        mLoggedCodexes.forEach(c -> c.parseFromNetworkTables());
    }

    /**
     * Makes a csv file and writes the starting row/header for each CodexNetworkTablesParser in mLoggedCodexes
     */
    public void logFromCodexToCSVHeader() {
        for (CodexNetworkTablesParser parser : mLoggedCodexes) {
            try {
                Writer logger = mWriters.get(parser.getCSVIdentifier());
                logger.append(parser.codexToCSVHeader());
                logger.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Logs codex values to its corresponding csv
     */
    public void logFromCodexToCSVLog() {
        for (CodexNetworkTablesParser parser : mLoggedCodexes) {
            try {
                Writer logger = mWriters.get(parser.getCSVIdentifier());
                logger.append(parser.codexToCSVLog());
                logger.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Closes all the writers in mWriters
     */
    public void closeWriters() {
        for (Writer writer : mWriters.values()) {
            try {
                writer.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Makes the log file if it doesn't already exist
     */
    private void handleCreation(File pFile) throws IOException {
        //Makes every folder before the file if the CSV's parent folder doesn't exist
        if(!pFile.getParentFile().exists()) {
            pFile.getParentFile().mkdirs();
        }

        //Creates the .CSV if it doesn't exist
        if(!pFile.exists()) {
            pFile.createNewFile();
        }
    }

    /**
     * Sends Codex entries into its corresponding NetworkTable
     */
    public void sendCodices() {
        mCodexNT.send(imu);
        mCodexNT.send(drive);
        mCodexNT.send("DRIVER", driverinput);
        mCodexNT.send("OPERATOR", operatorinput);
        mCodexNT.send(fourbar);
    }

    /**
     * Do this before sending codices to NetworkTables
     */
    public void registerCodices() {
        mCodexNT.registerCodex(EGyro.class);
        mCodexNT.registerCodex(EDriveData.class);
        mCodexNT.registerCodex("DRIVER", ELogitech310.class);
        mCodexNT.registerCodex("OPERATOR", ELogitech310.class);
        mCodexNT.registerCodex(EFourBarData.class);
    }
}