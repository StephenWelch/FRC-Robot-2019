package us.ilite.robot.commands;

import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;
import us.ilite.common.types.sensor.EGyro;
import us.ilite.robot.Data;
import us.ilite.common.lib.control.PIDController;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.geometry.Rotation2d;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class TurnToDegree implements ICommand {

  private ILog mLogger = Logger.createLog(this.getClass());

  private Drive mDrive;
  
  private static final int kMIN_ALIGNED_COUNT = 25;
  private static final double kTIMEOUT = 9999.9;
  private static final double kP = 0.001;
  private static final double kI = 0.0;
  private static final double kD = 0.0;
  private static final double kMIN_POWER = 0.0; //0.066666667
  private static final double kMAX_POWER = 1.0;
  private static final double kFrictionFeedforward = 0.085;
  
  private Rotation2d mInitialYaw, mTurnAngle, mTargetYaw;
  private double mOutput = 0.0;
  private double mStartTime;
  private double mPreviousTime;
  private int mAlignedCount;
  private PIDController pid;
  private final double mAllowableError;
  public Data mData;
  
  public TurnToDegree(Drive pDrive, Rotation2d pTurnAngle, double pAllowableError, Data pData) {
    this.mDrive = pDrive;
    
    this.mTurnAngle = pTurnAngle;
    this.mAllowableError = pAllowableError;

    this.mData = pData;
  }

  @Override
  public void init(double pNow) {
    mStartTime = pNow;

    mInitialYaw = getYaw();
    mTargetYaw = mInitialYaw.rotateBy( mTurnAngle );
    pid = new PIDController(kP, kI, kD);
    pid.setContinuous(true);
    pid.setOutputRange(kMIN_POWER, kMAX_POWER);
    pid.setInputRange( -180, 180 );
    pid.setSetpoint(mTargetYaw.getDegrees());
    pid.setOutputRange( -1, 1 );

    mPreviousTime = pNow;

    mAlignedCount = 0;
  }

  public boolean update(double pNow) {
    mOutput = pid.calculate(getYaw().getDegrees(), pNow - mPreviousTime);
    mOutput += Math.signum(mOutput) * kFrictionFeedforward;
    if ((Math.abs(pid.getError()) <= Math.abs(mAllowableError))) {
     mAlignedCount++;
    } else {
     mAlignedCount = 0;
    }
    if ( mAlignedCount >= kMIN_ALIGNED_COUNT || pNow - mStartTime > kTIMEOUT) {
      mDrive.setDriveMessage(new DriveMessage(0.0, 0.0, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));
      mLogger.info("Turn finished");
      mPreviousTime = System.currentTimeMillis();
      return true;
    }
    mDrive.setDriveMessage(new DriveMessage(mOutput, -mOutput, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake));
    Data.kSmartDashboard.putDouble("turn_error", pid.getError());
    mLogger.info("Target: " + mTargetYaw + " Yaw: " + getYaw() + "\n");
    mPreviousTime = pNow;
    return false;
  }
  
  private Rotation2d getYaw() {
    return Rotation2d.fromDegrees(mData.imu.get(EGyro.YAW_DEGREES));
  }
  
  @Override
  public void shutdown(double pNow) {
    
  }
 
}