/**
 * Robot and sensor simulations in the "EvaKontrol" page are created and updated in this class.
 */

package com.inovasyon.eva_mobil;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

import java.util.ArrayList;

class RobotCanvas extends View {

    /**
     * Sonar/Infrared sensor ranges from 0 to 11.
     */
    ArrayList<Integer> i_list_data = new ArrayList<>();

    /**
     * Height and width of the screen in px.
     */
    int i_total_height;
    int i_total_width;

    /**
     * One edge length of the robot.
     */
    float f_one_edge_distance;
    float f_one_edge_distance_for_width;
    float f_one_edge_distance_for_height;

    /**
     * Paint to draw robot border.
     */
    Paint paint = new Paint();

    /**
     * Paint to write applied velocities.
     */
    Paint paint_text = new Paint();

    /**
     * Paint to simulate sensor distances.
     */
    Paint paint_sensor = new Paint();

    /**
     * Stores robot boundries in x and y directions.
     */
    ArrayList<Float> f_list_x = new ArrayList<>();
    ArrayList<Float> f_list_y = new ArrayList<>();

    /**
     * Rectangle stores canvas area boundaries.
     */
    RectF rectF = new RectF();

    /**
     * Paint to fill progress bars' background.
     */
    Paint backgroundPaint;

    /**
     * Paint to fill forward, left and right velocities.
     */
    Paint foregroundPaint;

    /**
     * Paint to fill backward velocity.
     */
    Paint foregroundPaintRed;

    /**
     * Width of velocity progress bars.
     */
    float f_stroke;

    /**
     * This method runs automatically when the page is loaded.
     * @param context Activity context
     */
    public RobotCanvas(Context context) {
        super(context);
        /**
         * Init paints
         */
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setAlpha(125);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(2);

        foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaint.setColor(Color.GREEN);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeWidth(2);

        foregroundPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaintRed.setColor(Color.RED);
        foregroundPaintRed.setStyle(Paint.Style.STROKE);
        foregroundPaintRed.setStrokeWidth(2);

        paint_text.setTextAlign(Paint.Align.CENTER);
        paint_text.setColor(Color.rgb(253, 91, 10));
        paint_text.setTextSize(2);
    }

    /**
     * This method runs automatically and always.
     * @param canvas Canvas to add interface elements.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ComputeLengths();
        DrawRobotBoundries(canvas);
        DrawProgressBars(canvas);
        DrawSensorSimulation(canvas);
        AddRobotName(canvas);
        RobotCanvas.this.postInvalidate();
    }

    /**
     * Adds progress bars to the canvas.
     * @param canvas Canvas to add progress bars.
     */
    public void DrawProgressBars(Canvas canvas) {
        /**
         * Draw progress bars with default background color.
         */
        canvas.drawArc(rectF, 140, 260, false, backgroundPaint);
        canvas.drawLine(i_total_width / 2 + f_stroke, i_total_height - 2 * f_stroke, i_total_width - f_stroke, i_total_height - 2 * f_stroke, backgroundPaint);
        canvas.drawLine(i_total_width / 2 - f_stroke, i_total_height - 2 * f_stroke, f_stroke, i_total_height - 2 * f_stroke, backgroundPaint);

        /**
         * Write applied linear and angular velocities to the canvas.
         */
        canvas.drawText(String.valueOf(Math.round(EvaKontrol.f_applied_lin_speed*1000)), i_total_width / 2, 4 * f_stroke, paint_text);
        canvas.drawText(String.valueOf(Math.round(EvaKontrol.f_applied_rot_speed*1000)), i_total_width / 2, i_total_height - 3 * f_stroke, paint_text);

        /**
         * Progress bar values
         */
        float f_forward_backward;
        float f_right;
        float f_left;

        /**
         * Scale progress bar values from 0-100 to 0-260.
         */
        if(EvaKontrol.i_pb_forward>0)
            f_forward_backward = 260.0f*EvaKontrol.i_pb_forward/100.0f;
        else
            f_forward_backward = 260.0f*EvaKontrol.i_pb_backward/100.0f;
        if(f_forward_backward>260.0f)
            f_forward_backward = 260.0f;

        float f1 = i_total_width/2 + f_stroke;
        float f2 = i_total_width - f_stroke;

        f_left = Math.abs(f1-f2) * EvaKontrol.i_pb_left / 100.0f;
        f_right = Math.abs(f1-f2) * EvaKontrol.i_pb_right / 100.0f;

        /**
         * Fill progress bars with calculated values.
         */
        if(EvaKontrol.i_pb_forward>0)
            canvas.drawArc(rectF, 140, f_forward_backward, false, foregroundPaint);
        else if (EvaKontrol.i_pb_backward>0)
            canvas.drawArc(rectF, 140, f_forward_backward, false, foregroundPaintRed);
        canvas.drawLine(i_total_width / 2 + f_stroke, i_total_height - 2 * f_stroke, i_total_width / 2 + f_stroke + f_right, i_total_height - 2 * f_stroke, foregroundPaint);
        canvas.drawLine(i_total_width / 2 - f_stroke, i_total_height - 2 * f_stroke, i_total_width / 2 - f_stroke - f_left, i_total_height - 2 * f_stroke, foregroundPaint);
    }

    public void ComputeLengths()
    {
        /**
         * Width and height of canvas area.
         */
        i_total_height = EvaKontrol.lin_lay_canvas.getHeight();
        i_total_width = EvaKontrol.lin_lay_canvas.getWidth();

        /**
         * Calculate one edge distance.
         */
        f_one_edge_distance = 0;
        f_one_edge_distance_for_width = (float) (i_total_width / 7.73);
        f_one_edge_distance_for_height = (float) (i_total_height / 8.73);
        if (f_one_edge_distance_for_width < f_one_edge_distance_for_height)
            f_one_edge_distance = f_one_edge_distance_for_width;
        else
            f_one_edge_distance = f_one_edge_distance_for_height;
        float min = Math.min(i_total_width, i_total_height);

        /**
         * Paints are set with respect to stroke width.
         */
        f_stroke = f_one_edge_distance * 0.3f;
        rectF.set(0 + f_stroke, 0 + f_stroke, min - f_stroke, min - f_stroke);
        backgroundPaint.setStrokeWidth(2 * f_stroke);
        foregroundPaint.setStrokeWidth(2 * f_stroke);
        foregroundPaintRed.setStrokeWidth(2 * f_stroke);
        foregroundPaintRed.setShader(new RadialGradient(rectF.centerX(), rectF.centerY(), min / 2, new int[]{Color.rgb(0, 255, 0), Color.rgb(255, 0, 0)}, null, Shader.TileMode.MIRROR));
        foregroundPaint.setShader(new RadialGradient(rectF.centerX(), rectF.centerY(), min / 2, new int[]{Color.rgb(255, 0, 0), Color.rgb(0, 255, 0)}, null, Shader.TileMode.MIRROR));
        paint_text.setTextSize(f_one_edge_distance / 2);
    }

    /**
     * Returns cosinus of input degree
     * @param i_degree Input degree
     * @return Cosinus of input degree
     */
    public float Cosinus(int i_degree)
    {
        return (float)(Math.cos(((i_degree * Math.PI) / 180)));
    }

    /**
     * Returns sinus of input degree
     * @param i_degree Input degree
     * @return Sinus of input degree
     */
    public float Sinus(int i_degree)
    {
        return (float) (Math.sin(((i_degree * Math.PI) / 180)));
    }

    /**
     * Adds robot name to the canvas.
     * @param canvas Canvas to add robot name.
     */
    public void AddRobotName(Canvas canvas)
    {
        canvas.drawText(EvaKontrol.str_robot_name, canvas.getWidth() / 2, canvas.getHeight() / 2, paint_text);
    }

    /**
     * Draws sensor simulations.
     * @param canvas Canvas to add sensor simulation.
     */
    public void DrawSensorSimulation(Canvas canvas)
    {
        i_list_data.clear();
        final String[] separated = EvaKontrol.str_topic_msg.split(";");
        for (int i = 0; i < separated.length-1; i++)
            i_list_data.add(Math.round(Float.valueOf(separated[i])));

        paint_sensor.setColor(Color.GRAY);
        paint_sensor.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paint_sensor.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        paint_sensor.setPathEffect(new CornerPathEffect(15));
        Path sensorpath = new Path();
        float f_sensor_length = f_one_edge_distance * 0.2f;
        float f_space_length = f_one_edge_distance * 0.1f;
        int i_degree = 90;
        for(int i=0; i<12; i++)
        {
            /**
             * Background color is set with respect to range and limits.
             */
            paint_sensor.setColor(Color.GRAY);
            if(i_list_data.get(i)>0 && i_list_data.get(i)<=EvaKontrol.i_x1_range)
                paint_sensor.setColor(Color.RED);
            else if(i_list_data.get(i)>EvaKontrol.i_x1_range && i_list_data.get(i)<=EvaKontrol.i_x2_range)
                paint_sensor.setColor(Color.YELLOW);
            else if(i_list_data.get(i)>EvaKontrol.i_x2_range && i_list_data.get(i)<=5000)
                paint_sensor.setColor(Color.GREEN);

            /**
             * Sensor simulation is drawn to the edge.
             */
            sensorpath.reset();
            sensorpath.moveTo(f_list_x.get(i) - f_space_length * Cosinus(i_degree), f_list_y.get(i) - f_space_length * Sinus(i_degree));
            sensorpath.lineTo(f_list_x.get(i + 1) - f_space_length * Cosinus(i_degree), f_list_y.get(i + 1) - f_space_length * Sinus(i_degree));
            sensorpath.lineTo(f_list_x.get(i+1)-f_sensor_length*Cosinus(i_degree)- f_space_length * Cosinus(i_degree), f_list_y.get(i+1)-f_sensor_length*Sinus(i_degree)- f_space_length * Sinus(i_degree));
            sensorpath.lineTo(f_list_x.get(i)-f_sensor_length*Cosinus(i_degree)- f_space_length * Cosinus(i_degree), f_list_y.get(i)-f_sensor_length*Sinus(i_degree)- f_space_length * Sinus(i_degree));
            sensorpath.lineTo(f_list_x.get(i) - f_space_length * Cosinus(i_degree), f_list_y.get(i) - f_space_length * Sinus(i_degree));
            canvas.drawPath(sensorpath, paint_sensor);
            i_degree+=30;
        }

    }

    /**
     * Draws robot boundries to the canvas.
     * @param canvas Canvas to add robot boundries.
     */
    public void DrawRobotBoundries(Canvas canvas)
    {
        float f_x;
        float f_y;
        Path wallpath = new Path();

        /**
         * Robot boundries are drawn with blue colored paint.
         */
        f_list_x.clear();
        f_list_y.clear();
        f_x = 2*f_one_edge_distance_for_width + (float) (f_one_edge_distance_for_width * Cosinus(30) + f_one_edge_distance_for_width * Cosinus(60));
        f_y = 2*f_one_edge_distance_for_height;
        wallpath.reset();
        wallpath.moveTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_x = f_x + f_one_edge_distance;
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x);
        f_list_y.add(f_y);

        f_x = f_x + f_one_edge_distance * Cosinus(30);
        f_y = f_y + f_one_edge_distance * Sinus(30);
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x);
        f_list_y.add(f_y);

        f_x = f_x + f_one_edge_distance * Cosinus(60);
        f_y = f_y + f_one_edge_distance * Sinus(60);
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x);
        f_list_y.add(f_y);

        f_y = f_y + 2*f_one_edge_distance;
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_x = f_x - f_one_edge_distance * Cosinus(60);
        f_y = f_y + f_one_edge_distance * Sinus(60);
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_x = f_x - f_one_edge_distance * Cosinus(30);
        f_y = f_y + f_one_edge_distance * Sinus(30);
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_x = f_x - f_one_edge_distance;
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_x = f_x - f_one_edge_distance * Cosinus(30);
        f_y = f_y - f_one_edge_distance * Sinus(30);
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_x = f_x - f_one_edge_distance * Cosinus(60);
        f_y = f_y - f_one_edge_distance * Sinus(60);
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_y = f_y - f_one_edge_distance * 2;
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_x = f_x + f_one_edge_distance * Cosinus(60);
        f_y = f_y - f_one_edge_distance * Sinus(60);
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        f_x = f_x + f_one_edge_distance * Cosinus(30);
        f_y = f_y - f_one_edge_distance * Sinus(30);
        wallpath.lineTo(f_x, f_y);
        f_list_x.add(f_x); f_list_y.add(f_y);

        paint.setColor(Color.rgb(253,91,10));
        canvas.drawPath(wallpath, paint);
    }
}
