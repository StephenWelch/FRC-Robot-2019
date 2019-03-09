package us.ilite.robot.auto;

import com.google.gson.Gson;
import com.team254.lib.trajectory.timing.CentripetalAccelerationConstraint;
import us.ilite.common.AutonSelectionData;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryConstraints;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.common.types.auton.*;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.robot.auto.paths.DefaultAuto;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.middle.MidToMidLeftCargoToRocket;
import us.ilite.robot.auto.paths.middle.MidToMidRightCargoToRocket;
import us.ilite.robot.commands.*;
import us.ilite.robot.modules.*;

public class AutonomousRoutines {

    public static final TrajectoryConstraints kDefaultTrajectoryConstraints = new TrajectoryConstraints(
            100.0,
            40.0,
            12.0,
            new CentripetalAccelerationConstraint(20.0)
    );

    private TrajectoryGenerator mTrajectoryGenerator;

    private Drive mDrive;
    private Elevator mElevator;
    private Intake mIntake;
    private CargoSpit mCargoSpit;
    private HatchFlower mHatchFlower;
    private Limelight mLimelight;
    private VisionGyro mVisionGyro;
    private Data mData;

    private AutoSequence mDefaultAuto;

    private AutoSequence mMid_MidLeftCargo_LeftRocket;//TODO these
    private AutoSequence mMid_MidRightCargo_LeftRocket;//TODO these
    private AutoSequence mMid_MidRightCargo_LeftCargoShip;//TODO these
    private AutoSequence mMid_MidLeftCargo_LeftCargoShip;//TODO these

    private ICommand[] mMid_MidLeftCargo_LeftRocket_Sequence; //TODO these
    private ICommand[] mMid_MidLeftCargo_LeftCargoShip_Sequence;//TODO these
    private ICommand[] mMid_MidRightCargo_LeftRocket_Sequence;//TODO these
    private ICommand[] mMid_MidRightCargo_LeftCargoShip_Sequence;//TODO these

    private Gson mGson = new Gson();

    public AutonomousRoutines(TrajectoryGenerator mTrajectoryGenerator, Drive mDrive, Elevator mElevator, PneumaticIntake mPneumaticIntake, Intake mIntake, CargoSpit mCargoSpit, HatchFlower mHatchFlower, Limelight mLimelight, VisionGyro mVisionGyro, Data mData) {
        this.mTrajectoryGenerator = mTrajectoryGenerator;
        this.mDrive = mDrive;
        this.mElevator = mElevator;
        this.mIntake = mIntake;
        this.mCargoSpit = mCargoSpit;
        this.mHatchFlower = mHatchFlower;
        this.mLimelight = mLimelight;
        this.mVisionGyro = mVisionGyro;
        this.mData = mData;

        mMid_MidLeftCargo_LeftRocket = new MidToMidLeftCargoToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro, mCargoSpit);
        mMid_MidRightCargo_LeftRocket = new MidToMidRightCargoToRocket(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mLimelight, mVisionGyro)
    }

    public void generateTrajectories() {
        //Cargo Sequences
        mMiddleToMiddleCargoToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateCargoSequence();
        mMiddleToMiddleCargoToSideCargoSequence = mMiddleToMiddleCargoToSideCargo.generateCargoSequence();
        mMiddleToMiddleCargoMiddleCargoSequence = mMiddleToMiddleCargoToMiddleCargo.generateCargoSequence();

        //---------------------------------------------------------------------------------------------
        mMid_MidLeftCargo_LeftCargoShip_Sequence = mMid_MidLeftCargo_LeftRocket.generateCargoSequence();
        mMid_MidLeftCargo_LeftRocket_Sequence = mMid_MidLeftCargo_LeftCargoShip.generateCargoSequence();
        mMid_MidRightCargo_LeftCargoShip_Sequence = mMid_MidRightCargo_LeftCargoShip.generateCargoSequence();
        mMid_MidRightCargo_LeftRocket_Sequence = mMid_MidRightCargo_LeftRocket.generateCargoSequence();
        //---------------------------------------------------------------------------------------------


        //Hatch Sequences
        mMiddleToMiddleHatchToSideRocketSequence = mMiddleToMiddleCargoToSideRocket.generateHatchSequence();
        mMiddleToMiddleCargoToSideCargoSequence = mMiddleToMiddleCargoToSideCargo.generateHatchSequence();
        mMiddleToMiddleCargoMiddleCargoSequence = mMiddleToMiddleCargoToMiddleCargo.generateCargoSequence();
    }

    public ICommand[] getDefault() {
        return mMiddleToMiddleCargoToSideRocket.generateCargoSequence();
    }

    public ICommand[] getSequence() {
        Integer cargoShipAction = (int)Data.kAutonTable.getEntry( ECargoShipAction.class.getSimpleName() ).getDouble(99);
        Integer hatchShipAction = (int)Data.kAutonTable.getEntry( EHatchShipAction.class.getSimpleName() ).getDouble(99);;
        Integer cargoRocketAction = (int)Data.kAutonTable.getEntry( ECargoRocketAction.class.getSimpleName() ).getDouble( 99 );
        Integer hatchRocketAction = (int)Data.kAutonTable.getEntry( EHatchRocketAction.class.getSimpleName() ).getDouble( 99 );
        Integer startingPosition = (int)Data.kAutonTable.getEntry( EStartingPosition.class.getSimpleName() ).getDouble( 99 );
        AutonSelectionData data = mGson.fromJson("", AutonSelectionData.class);
        
        switch(EStartingPosition.intToEnum( startingPosition )) {
            case LEFT:
                switch (ECargoShipAction.intToEnum( cargoShipAction )) {
                    case FRONT_LEFT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case FRONT:
                                break;
                            case LEFT:
                                break;
                            case RIGHT:
                                break;
                            case NONE: // assume we go to the closest side cargo, TBD in future
                                break;
                            default:
                                break;
                        }
                    default:
                        break;
                }
//                switch (EHatchShipAction.intToEnum( hatchShipAction )) {
//                    case FRONT:
//                        break;
//                    case LEFT:
//                        break;
//                    case RIGHT:
//                        break;
//                }
//                switch (EHatchRocketAction.intToEnum( hatchRocketAction )) {
//                    case FRONT:
//                        break;
//                    case LEFT:
//                        break;
//                    case RIGHT:
//                        break;
//                }
//                break;
            case RIGHT:
                switch (ECargoShipAction.intToEnum( cargoShipAction )) {
                    case FRONT_RIGHT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case FRONT:
                                break;
                            case LEFT:
                                break;
                            case RIGHT:
                                break;
                            case NONE: // assume we go to the closest side cargo, TBD in future
                                break;
                            default:
                                break;
                        }
                    default:
                        break;
                }
//
//                switch (EHatchShipAction.intToEnum( hatchShipAction )) {
//                    case FRONT:
//                        break;
//                    case LEFT:
//                        break;
//                    case RIGHT:
//                        break;
//                }
//                switch (EHatchRocketAction.intToEnum( hatchRocketAction )) {
//                    case FRONT:
//                        break;
//                    case LEFT:
//                        break;
//                    case RIGHT:
//                        break;
//                }
//                break;

            case MIDDLE:
                switch (ECargoShipAction.intToEnum( cargoShipAction )) {
                    case FRONT_LEFT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case FRONT:
                                break;
                            case LEFT:
                                break;
                            case RIGHT:
                                break;
                            case NONE: // assume we go to the closest side cargo, TBD in future
                                break;
                            default:
                                break;
                        }
                    case FRONT_RIGHT:
                        switch (ECargoRocketAction.intToEnum( cargoRocketAction )) {
                            case FRONT:
                                break;
                            case LEFT:
                                break;
                            case RIGHT:
                                break;
                            case NONE:
                                break;
                            default:
                                break;
                        }
                        default:
                            break;
                }
//
//                switch (EHatchShipAction.intToEnum( hatchShipAction )) {
//                    case FRONT:
//                        break;
//                    case LEFT:
//                        break;
//                    case RIGHT:
//                        break;
//                }
//                switch (EHatchRocketAction.intToEnum( hatchRocketAction )) {
//                    case FRONT:
//                        break;
//                    case LEFT:
//                        break;
//                    case RIGHT:
//                        break;
//                }

            case UNKNOWN:
                break;
            default:
                return mDefaultAuto.generateDefaultSequence();
        }

        return null;

    }

}
