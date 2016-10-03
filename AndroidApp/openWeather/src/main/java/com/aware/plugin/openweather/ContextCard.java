package com.aware.plugin.openweather;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aware.plugin.openweather.Provider.OpenWeather_Data;
import com.aware.utils.IContextCard;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ContextCard implements IContextCard {

    /**
     * Constructor for Stream reflection
     */
    public ContextCard(){}

	public View getContextCard(Context context) {

        LayoutInflater sInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View card = sInflater.inflate(R.layout.layout, null);

		ImageView weather_icon = (ImageView) card.findViewById(R.id.icon_weather);
		TextView weather_city = (TextView) card.findViewById(R.id.weather_city);
        TextView weather_description = (TextView) card.findViewById(R.id.weather_description);
        TextView weather_temperature = (TextView) card.findViewById(R.id.weather_temperature);
        TextView weather_max_temp = (TextView) card.findViewById(R.id.weather_max_temp);
        TextView weather_min_temp = (TextView) card.findViewById(R.id.weather_min_temp);
        TextView weather_pressure = (TextView) card.findViewById(R.id.weather_pressure);
        TextView weather_humidity = (TextView) card.findViewById(R.id.weather_humidity);
        TextView weather_cloudiness = (TextView) card.findViewById(R.id.weather_cloudiness);
        TextView weather_wind = (TextView) card.findViewById(R.id.weather_wind);
        TextView weather_wind_degrees = (TextView) card.findViewById(R.id.weather_wind_degrees);
        TextView weather_rain = (TextView) card.findViewById(R.id.rain);
        TextView weather_snow = (TextView) card.findViewById(R.id.snow);
        TextView sunrise = (TextView) card.findViewById(R.id.sunrise);
        TextView sunset = (TextView) card.findViewById(R.id.sunset);

        LineChart weather_plot = (LineChart) card.findViewById(R.id.temp_plot);

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(System.currentTimeMillis());
        int current_hour = cal.get(Calendar.HOUR_OF_DAY);
        boolean is_daytime = ( current_hour >= 8 && current_hour <= 18 );

        Cursor latest_weather = context.getContentResolver().query( OpenWeather_Data.CONTENT_URI, null, null, null, OpenWeather_Data.TIMESTAMP + " DESC LIMIT 1" );
        if( latest_weather != null && latest_weather.moveToFirst() ) {
            int weather_id = latest_weather.getInt(latest_weather.getColumnIndex(OpenWeather_Data.WEATHER_ICON_ID));
            if( weather_id >= 200 && weather_id <= 232 ) {
                weather_icon.setImageResource(R.drawable.ic_weather_thunderstorm);
            } else if( weather_id >= 300 && weather_id <= 321 ) {
                weather_icon.setImageResource(R.drawable.ic_weather_drizzle);
            } else if( weather_id >= 500 && weather_id <= 531 ) {
                weather_icon.setImageResource(R.drawable.ic_weather_rain);
            } else if( weather_id >= 600 && weather_id <= 622 ) {
                weather_icon.setImageResource(R.drawable.ic_weather_snow);
            } else if( weather_id == 906 ) {
                weather_icon.setImageResource(R.drawable.ic_weather_hail);
            } else if( weather_id >= 701 && weather_id <= 781 ) {
                if( is_daytime ) {
                    weather_icon.setImageResource(R.drawable.ic_weather_fog_day);
                } else {
                    weather_icon.setImageResource(R.drawable.ic_weather_fog_night);
                }
            } else if( weather_id >= 801 && weather_id <= 803 ) {
                weather_icon.setImageResource(R.drawable.ic_weather_cloudy);
            } else {
                if( is_daytime ) {
                    weather_icon.setImageResource(R.drawable.ic_weather_clear_day);
                } else {
                    weather_icon.setImageResource(R.drawable.ic_weather_clear_night);
                }
            }
            weather_city.setText(latest_weather.getString(latest_weather.getColumnIndex(OpenWeather_Data.CITY)));
            weather_description.setText(latest_weather.getString(latest_weather.getColumnIndex(OpenWeather_Data.WEATHER_DESCRIPTION)));
            weather_temperature.setText(String.format("%.1f",latest_weather.getDouble(latest_weather.getColumnIndex(OpenWeather_Data.TEMPERATURE))) + "º");
            weather_min_temp.setText(context.getResources().getString(R.string.label_minimum) + String.format(" %.1f",latest_weather.getDouble(latest_weather.getColumnIndex(OpenWeather_Data.TEMPERATURE_MIN))));
            weather_max_temp.setText(context.getResources().getString(R.string.label_maximum) + String.format(" %.1f",latest_weather.getDouble(latest_weather.getColumnIndex(OpenWeather_Data.TEMPERATURE_MAX))));
            weather_pressure.setText(latest_weather.getDouble(latest_weather.getColumnIndex(OpenWeather_Data.PRESSURE))+ " hPa");
            weather_humidity.setText(latest_weather.getInt(latest_weather.getColumnIndex(OpenWeather_Data.HUMIDITY)) + " %");
            weather_cloudiness.setText(latest_weather.getInt(latest_weather.getColumnIndex(OpenWeather_Data.CLOUDINESS)) + " %");
            weather_wind.setText(latest_weather.getFloat(latest_weather.getColumnIndex(OpenWeather_Data.WIND_SPEED)) + " m/s");
            weather_wind_degrees.setText(latest_weather.getInt(latest_weather.getColumnIndex(OpenWeather_Data.WIND_DEGREES)) + "º");
            weather_rain.setText(latest_weather.getInt(latest_weather.getColumnIndex(OpenWeather_Data.RAIN)) + " mm");
            weather_snow.setText(latest_weather.getInt(latest_weather.getColumnIndex(OpenWeather_Data.SNOW)) + " mm");
            sunrise.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(latest_weather.getInt(latest_weather.getColumnIndex(OpenWeather_Data.SUNRISE)) * 1000L)));
            sunset.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(latest_weather.getInt(latest_weather.getColumnIndex(OpenWeather_Data.SUNSET)) * 1000L)));
        }
        if( latest_weather != null && ! latest_weather.isClosed() ) latest_weather.close();

        drawGraph(context, weather_plot);

		return card;
	}

    private LineChart drawGraph( Context context, LineChart mChart ) {
        //Get today's time from the beginning in milliseconds
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        ArrayList<String> x_hours = new ArrayList<>();
        for(int i=0; i<24; i++) {
            x_hours.add(String.valueOf(i));
        }

        ArrayList<Entry> entries = new ArrayList<>();
        //add frequencies to the right hour buffer
        Cursor weatherData = context.getContentResolver().query(OpenWeather_Data.CONTENT_URI, new String[]{ OpenWeather_Data.TEMPERATURE,"strftime('%H',"+ OpenWeather_Data.TIMESTAMP + "/1000, 'unixepoch', 'localtime')+0 as time_of_day" }, OpenWeather_Data.TIMESTAMP + " >= " + c.getTimeInMillis() + " ) GROUP BY ( time_of_day ", null, "time_of_day ASC");
        if( weatherData != null && weatherData.moveToFirst() ) {
            do{
                entries.add( new Entry(weatherData.getInt(0), weatherData.getInt(1)) );
            } while( weatherData.moveToNext() );
        }
        if( weatherData != null && ! weatherData.isClosed()) weatherData.close();

        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        dataSet.setColor(Color.parseColor("#33B5E5"));
        dataSet.setDrawValues(false);

        LineData data = new LineData(x_hours, dataSet);

        mChart.setContentDescription("");
        mChart.setDescription("");

        ViewGroup.LayoutParams params = mChart.getLayoutParams();
        params.height = 200;
        mChart.setLayoutParams(params);

        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBorders(false);

        YAxis left = mChart.getAxisLeft();
        left.setDrawLabels(true);
        left.setDrawGridLines(true);
        left.setDrawAxisLine(true);

        YAxis right = mChart.getAxisRight();
        right.setDrawAxisLine(false);
        right.setDrawLabels(false);
        right.setDrawGridLines(false);

        XAxis bottom = mChart.getXAxis();
        bottom.setPosition(XAxis.XAxisPosition.BOTTOM);
        bottom.setSpaceBetweenLabels(0);
        bottom.setDrawGridLines(false);

        mChart.setData(data);
        mChart.invalidate();
        mChart.animateX(1000);

        return mChart;
    }
}
