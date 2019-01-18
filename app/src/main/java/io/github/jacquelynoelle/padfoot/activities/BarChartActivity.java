package io.github.jacquelynoelle.padfoot.activities;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import io.github.jacquelynoelle.padfoot.R;

public class BarChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        BarChart chart = (BarChart) findViewById(R.id.chart1);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setScaleXEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDragDecelerationEnabled(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 3));
        entries.add(new BarEntry(2, 1));
        entries.add(new BarEntry(3, 2));
        entries.add(new BarEntry(4, 3));
        entries.add(new BarEntry(5, 4));
        entries.add(new BarEntry(6, 5));
        entries.add(new BarEntry(7, 3));
        entries.add(new BarEntry(8, 1));
        entries.add(new BarEntry(9, 2));
        entries.add(new BarEntry(10, 3));
        entries.add(new BarEntry(11, 4));
        entries.add(new BarEntry(12, 5));
        entries.add(new BarEntry(13, 3));
        entries.add(new BarEntry(14, 1));
        entries.add(new BarEntry(15, 2));
        entries.add(new BarEntry(16, 3));
        entries.add(new BarEntry(17, 0));
        entries.add(new BarEntry(18, 0));
        entries.add(new BarEntry(19, 0));
        entries.add(new BarEntry(20, 0));
        entries.add(new BarEntry(21, 0));
        entries.add(new BarEntry(22, 0));
        entries.add(new BarEntry(23, 0));
        entries.add(new BarEntry(24, 0));

        BarDataSet barDataSet = new BarDataSet(entries, "Cells");
        barDataSet.setHighlightEnabled(true);
        barDataSet.setDrawValues(false);
        barDataSet.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        barDataSet.setHighLightAlpha(255);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0);
        xAxis.setLabelCount(6);

        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);

        chart.getLegend().setEnabled(false);   // Hide the legend

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Dec");
        labels.add("Nov");
        labels.add("Oct");
        labels.add("Sep");
        labels.add("Aug");
        labels.add("Jul");
        labels.add("Jun");
        labels.add("May");
        labels.add("Apr");
        labels.add("Mar");
        labels.add("Feb");
        labels.add("Jan");

        BarData data = new BarData(barDataSet);
        chart.setData(data); // set the data and list of lables into chart

        Description description = new Description();
        description.setText("");
        chart.setDescription(description); // set the description
        chart.setDrawBorders(false);

        int[] colors = new int[]{
                R.color.colorAccent,
                R.color.colorAccentLight,
        };
        barDataSet.setColors(ColorTemplate.createColors(getResources(), colors));

        chart.invalidate();
        chart.animateY(3000);
    }
}
