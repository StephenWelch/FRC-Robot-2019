package us.ilite.lib.drivers;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.kauailabs.navx.frc.AHRS;

import com.team254.lib.geometry.Rotation2d;
import edu.wpi.first.wpilibj.SerialPort.Port;
import us.ilite.common.config.SystemSettings;

public class NavX extends IMU {

	private static final ILog mLog = Logger.createLog(NavX.class);

 	private double initialAngle;
 	private final AHRS iahrs;
  
   //TODO - single value for now - could be VERY noisy
   // others to try: {0.75, 0.25}, {0.6, 0.4}, {0.5, 0.3, 0.2}
   private static final double[] kCollisionGains = {1.0};
	
 	public NavX(Port pPort){
 	  super(kCollisionGains);
 	  iahrs = new AHRS(pPort, AHRS.SerialDataType.kProcessedData, (byte) SystemSettings.kGyroUpdateRate);
 	  mLog.info("NavX Update Rate: " + iahrs.getActualUpdateRate());
 	}

 	public double getInitialAngle() {
 		return initialAngle;
 	}

 	public double getYaw() {
 		return iahrs.getYaw();
 	}

	 @Override
	 public Rotation2d getHeading() {
		 return Rotation2d.fromDegrees(iahrs.getYaw());
	 }

	 public double getDisplacementX() {
 		return iahrs.getDisplacementX();
 	}
	
 	public double getDisplacementY() {
 		return iahrs.getDisplacementY();
 	}
	
 	public double getDisplacementZ() {
 		return iahrs.getDisplacementZ();
 	}

 	public void resetDisplacement() {
 		iahrs.resetDisplacement();
 	}
	
 	public boolean isCalibrating(){
 		return iahrs.isCalibrating();
 	}
		
 	public double getAngle(){
 		return iahrs.getAngle();
 	}
	
 	public double getAngleOffStart(){
 		return getAngleSum(getAngle(), -initialAngle);
 	}
	
 	 public static double getAngleSum(double pRawValue1, double pRawValue2) {
 		    double sum = pRawValue1 + pRawValue2;
 		    if(sum > 180){
 		      sum = -360 + sum;
 		    } else if(sum < -180){
 		      sum = 360 + sum;
 		    }
 		    return sum;
 		  }
	
	
 	public void setInitialAngle(double yaw){
 		initialAngle = yaw;
 	}

   @Override
   public double getPitch() {
     return iahrs.getPitch();
   }

   @Override
   public double getRoll() {
     return iahrs.getRoll();
   }

   @Override
   public void zeroAll() {
     iahrs.reset();
   }

   @Override
   protected double getRawAccelX() {
     return iahrs.getRawAccelX();
   }

   @Override
   protected double getRawAccelY() {
     return iahrs.getRawAccelY();
   }

   @Override
   protected void updateSensorCache(double pTimestampNow) {
     // TODO Auto-generated method stub
    
   }

 }
