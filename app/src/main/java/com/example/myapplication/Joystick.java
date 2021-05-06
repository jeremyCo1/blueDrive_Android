package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Joystick {

    private int outerCircleRadius;
    private int innerCircleRadius;
    private int outerCircleCenterPositionX;
    private int outerCircleCenterPositionY;
    private int innerCircleCenterPositionX;
    private int innerCircleCenterPositionY;
    private Paint outerCirclePaint;
    private Paint innerCirclePaint;
    private double joystickCenterToTouchDistance;
    private boolean isPressed;
    private double actuatorX;
    private double actuatorY;

    public Joystick(int centerPositionsX, int centerPositionsY,int outerCircleRadius, int innerCircleRadius) {

        //Outer and inner circle make up the joystick
        this.outerCircleCenterPositionX = centerPositionsX;
        this.outerCircleCenterPositionY = centerPositionsY;
        this.innerCircleCenterPositionX = centerPositionsX;
        this.innerCircleCenterPositionY = centerPositionsY;

        //Radii of circles
        this.outerCircleRadius = outerCircleRadius;
        this.innerCircleRadius = innerCircleRadius;

        //paint of circles
        this.outerCirclePaint = new Paint();
        this.outerCirclePaint.setColor(Color.GRAY);
        this.outerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        this.innerCirclePaint = new Paint();
        this.innerCirclePaint.setColor(Color.GRAY);
        this.innerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    public void draw(Canvas canvas) {

        //Draw outer circle
        canvas.drawCircle(
                this.outerCircleCenterPositionX,
                this.outerCircleCenterPositionY,
                this.outerCircleRadius,
                this.outerCirclePaint
        );

        //Draw inner circle
        canvas.drawCircle(
                this.innerCircleCenterPositionX,
                this.innerCircleCenterPositionY,
                this.innerCircleRadius,
                this.innerCirclePaint
        );
    }

    public void update(){
        upatdeInnerCirclePosition();
    }

    private void upatdeInnerCirclePosition(){
        this.innerCircleCenterPositionX = (int) (this.outerCircleCenterPositionX + this.actuatorX*this.outerCircleRadius);
        this.innerCircleCenterPositionY = (int) (this.outerCircleCenterPositionY + this.actuatorY*this.outerCircleRadius);
    }

    public boolean isPressed(double touchePositionX, double touchePositionY){
        this.joystickCenterToTouchDistance = Math.sqrt(
                Math.pow(this.outerCircleCenterPositionX - touchePositionX, 2) +
                        Math.pow(this.outerCircleCenterPositionY - touchePositionY, 2)
        );
        return this.joystickCenterToTouchDistance < this.outerCircleRadius;
    }

    public void setIsPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }

    public boolean getIsPressed(){
        return this.isPressed;
    }

    public void setActuator(double touchePositionX, double touchePositionY) {
        double deltaX = touchePositionX - this.outerCircleCenterPositionX;
        double deltaY = touchePositionY - this.outerCircleCenterPositionY;
        double deltaDistance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));

        if (deltaDistance < this.outerCircleRadius) {
            this.actuatorX = deltaX/this.outerCircleRadius;
            this.actuatorY = deltaY/this.outerCircleRadius;
        } else {
            this.actuatorX = deltaX/deltaDistance;
            this.actuatorY = deltaY/deltaDistance;
        }
    }

    public void resetActuator() {
        this.actuatorX = 0.0;
        this.actuatorY = 0.0;
    }

    public double getActuatorX(){
        return this.actuatorX;
    }

    public double getActuatorY(){
        return this.actuatorY;
    }

}
