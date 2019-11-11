/*Copyright 2019

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.TouchSensor;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import com.qualcomm.hardware.bosch.BNO055IMU;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

@Disabled
public class LaBarca {

  //Inicializar variables para motores y sensores del robot
  public DcMotor leftDrive = null;
  public DcMotor rightDrive = null;
  public DistanceSensor distanceSensor = null;
  public TouchSensor boton = null;
  private LinearOpMode programa;
  public DcMotor intakeLeft = null;
  public DcMotor intakeRight = null;
  public DcMotor elevadorLeft = null;
  public DcMotor elevadorRight = null;
  public DcMotor foundationLeft = null;
  public DcMotor foundationRight = null;

  private BNO055IMU imu;
  private Orientation angles;
  private Acceleration gravity;

  public LaBarca(LinearOpMode programa){
    this.programa = programa;
  }

  //Metodo para buscar motores y servomotores del Expansion y asignarlos a las variables
  public void getHardware(HardwareMap hwMap){
      leftDrive = hwMap.get(DcMotor.class, "motor_left");
      rightDrive = hwMap.get(DcMotor.class, "motor_right");
      intakeLeft = hwMap.get(DcMotor.class, "intake1");
      intakeRight = hwMap.get(DcMotor.class, "intake2");
      elevadorLeft = hwMap.get(DcMotor.class, "elevador1");
      elevadorRight = hwMap.get(DcMotor.class, "elevador2");
      foundationLeft = hwMap.get(DcMotor.class, "foundation1");
      foundationRight = hwMap.get(DcMotor.class, "foundation2");
      boton = hwMap.get(TouchSensor.class, "boton");

      leftDrive.setDirection(DcMotor.Direction.REVERSE);
      rightDrive.setDirection(DcMotor.Direction.FORWARD);
      intakeRight.setDirection(DcMotor.Direction.REVERSE);
      elevadorRight.setDirection(DcMotor.Direction.REVERSE);
      foundationLeft.setDirection(DcMotor.Direction.REVERSE);

      //intakeLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      //intakeRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

      BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
      parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
      parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
      parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
      parameters.loggingEnabled      = true;
      parameters.loggingTag          = "IMU";
      parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

      imu = hwMap.get(BNO055IMU.class, "imu");
      imu.initialize(parameters);
      imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);
  }

  public void frenar(){
    leftDrive.setPower(0);
    rightDrive.setPower(0);
  }

  public void resetEncoders(){
    leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    defaultRunmode();
  }

  public void defaultRunmode(){
    rightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    leftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
  }

  public double getDesviacion(){
    angles   = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
    gravity  = imu.getGravity();
    return angles.firstAngle;
  }

  /* Metodos para programacion autonoma */

  //Este metodo movera al robot en linea recta la distancia que se especifique
  public void moverDistanciaRecta(double distancia){
    if(!programa.opModeIsActive()) return;
      //Convertir rotaciones a ticks del encoder del Core Hex
      //9cm de llanta con engrane de 72 y uno de 125 en el motor
      int counts = (int) Math.round(560d * distancia / 9d / Math.PI);

      //Establecer la posicion actual del encoder como nuestro cero
      resetEncoders();

      //Establecer a que posicion y velocidad se debe mover el robot
      leftDrive.setTargetPosition(counts);
      rightDrive.setTargetPosition(counts);
      leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      leftDrive.setPower(1);
      rightDrive.setPower(1);

      //Cambiar el modo del motor para comenzar movimiento automatico
      while(programa.opModeIsActive()){
        programa.telemetry.addData("Right encoder:", rightDrive.getCurrentPosition());
        programa.telemetry.addData("Left encoder:", leftDrive.getCurrentPosition());
        programa.telemetry.addData("Target:", counts);
        programa.telemetry.update();
        if(!(leftDrive.isBusy() && rightDrive.isBusy())){
          frenar();
          break;
        }
      }
      defaultRunmode();
  }

  public void setGiroDeNoventaGrados(int desiredPosition){

  }

  public void girarEnEje(double distancia) {
    if(!programa.opModeIsActive()) return;
      int counts = (int) Math.round(560d * distancia / 9d / Math.PI);

      //Establecer la posicion actual del encoder como nuestro cero
      resetEncoders();

      //Establecer a que posicion y velocidad se debe mover el robot
      leftDrive.setTargetPosition(-counts);
      rightDrive.setTargetPosition(counts);
      leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      leftDrive.setPower(1);
      rightDrive.setPower(1);

      //Cambiar el modo del motor para comenzar movimiento automatico
      while(programa.opModeIsActive()){
        programa.telemetry.addData("Right encoder:", rightDrive.getCurrentPosition());
        programa.telemetry.addData("Left encoder:", leftDrive.getCurrentPosition());
        programa.telemetry.addData("Target:", counts);
        programa.telemetry.update();
        if(!(leftDrive.isBusy() && rightDrive.isBusy())){
          frenar();
          break;
        }
      }
      defaultRunmode();
  }

  public void activarElevador(double power) {
    final double velocidadSubida = 0.5;
    final double velocidadBajada = 0.5;
    if(power > 0){
      elevadorRight.setPower(velocidadSubida);
      elevadorLeft.setPower(velocidadSubida);
    } else if (power < 0){
      elevadorRight.setPower(-velocidadBajada);
      elevadorLeft.setPower(-velocidadBajada);
    } else {
      elevadorRight.setPower(0);
      elevadorLeft.setPower(0);
    }
  }

  public void activarIntake(double power){
      final double velocidad = 0.6;
    if(power > 0 && !boton.isPressed()){
      intakeLeft.setPower(velocidad);
      intakeRight.setPower(velocidad);
    } else if(power < 0){
      intakeRight.setPower(-velocidad);
      intakeLeft.setPower(-velocidad);
    } else {
      intakeRight.setPower(0);
      intakeLeft.setPower(0);
    }
  }

  public void activarFoundation(double power) {
    final double velocidad = 0.4;
    if(power > 0 && foundationLeft.getCurrentPosition() < 144 && foundationRight.getCurrentPosition() < 144) {
      foundationLeft.setPower(velocidad);
      foundationRight.setPower(velocidad);
    } else if(power < 0 && foundationLeft.getCurrentPosition() >= 0 && foundationRight.getCurrentPosition() >= 0) {
      foundationLeft.setPower(-velocidad);
      foundationRight.setPower(-velocidad);
    } else {
      foundationLeft.setPower(0);
      foundationRight.setPower(0);
    }
  }
}
