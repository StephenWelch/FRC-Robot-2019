package us.ilite.robot;

import edu.wpi.first.wpilibj.Notifier;
import us.ilite.common.Data;
import us.ilite.common.config.SystemSettings;

public class CSVLogger implements Runnable {
    private Notifier mLoggingNotifier;
    private Data mData;

    public CSVLogger( Data pData ) {
        mData = pData;
        mLoggingNotifier = new Notifier( this );
    }

    /**
     * Starts the periodically called logging by mLoggingNotifier
     */
    public void start() {
        mData.logFromCodexToCSVHeader();
        mLoggingNotifier.startPeriodic( SystemSettings.kCSVLoggingPeriod );
    }

    /**
     * Stops the periodically called logging by mLoggingNotifier
     */
    public void stop() {
        mLoggingNotifier.stop();
        mData.closeWriters();
    }

    public void run() {
        mData.logFromCodexToCSVLog();
    }

}